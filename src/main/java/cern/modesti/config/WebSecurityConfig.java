/**
 *
 */
package cern.modesti.config;

import org.apache.catalina.Context;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;

import cern.modesti.security.ldap.LdapSynchroniser;
import cern.modesti.security.ldap.LdapUserDetailsMapper;

/**
 * TODO
 *
 * Web security and LDAP configuration beans.
 *
 * The authentication/authorisation mechanism uses two different LDAP servers:
 * cerndc.cern.ch and xldap.cern.ch. The former is available in authenticated
 * mode only, from inside CERN only, and is used to authenticate a user. The
 * latter is available anonymously, from inside CERN only, and is used to
 * perform anonymous lookups on the LDAP server in order to do things like
 * lookup all the e-groups a particular user is a member of, or to check if a
 * user is a member of a particular e-group.
 *
 * See <a href=
 * "https://espace.cern.ch/identitymanagement/Wiki%20Pages/Active%20Directory%20Publication.aspx"
 * >this page</a> for documentation about the CERN LDAP structure.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
@Profile({"dev", "prod"})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  Environment env;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authenticationProvider(ldapAuthenticationProvider());

    http
        // Enable basic HTTP authentication
        .httpBasic()
        // Authentication is required for all URLs
        .and().authorizeRequests().anyRequest().authenticated()
        // TODO: implement CSRF protection. Here we just turn it off.
        .and().csrf().disable();
  }

  /**
   * Synchronise LDAP users and groups at startup via an InitializingBean.
   *
   * @param ldapSynchroniser the newly created LdapSynchroniser instance
   * @return the initialising bean
   */
  @Bean
  InitializingBean usersAndGroupsInitializer(final LdapSynchroniser ldapSynchroniser) {
    return ldapSynchroniser::synchroniseUsersAndGroups;
  }

  @Bean
  public LdapAuthenticationProvider ldapAuthenticationProvider() {
    LdapAuthenticationProvider provider = new LdapAuthenticationProvider(ldapAuthenticator());
    provider.setUserDetailsContextMapper(ldapUserDetailsMapper());
    return provider;
  }

  @Bean
  public LdapUserDetailsMapper ldapUserDetailsMapper() {
    return new LdapUserDetailsMapper();
  }

  @Bean
  public LdapAuthenticator ldapAuthenticator() {
    BindAuthenticator authenticator = new BindAuthenticator(contextSource());
    String[] userDnPatterns = new String[] { env.getRequiredProperty("ldap.user.filter") };
    authenticator.setUserDnPatterns(userDnPatterns);
    return authenticator;
  }

  @Bean
  public LdapContextSource contextSource() {
    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(env.getRequiredProperty("ldap.auth.url"));
    contextSource.setBase(env.getRequiredProperty("ldap.base"));
    return contextSource;
  }

  @Bean
  public LdapTemplate anonymousLdapTemplate() {
    return new SpringSecurityLdapTemplate(anonymousContextSource());
  }

  @Bean
  public LdapContextSource anonymousContextSource() {
    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(env.getRequiredProperty("ldap.anon.url"));
    contextSource.setBase(env.getRequiredProperty("ldap.base"));
    contextSource.setAnonymousReadOnly(true);
    return contextSource;
  }

  @Bean
  public LoggerListener loggerListener() {
    return new LoggerListener();
  }

  /**
   * This class enables programmatic customisation of the Tomcat context used by Spring Boot.
   */
  @Configuration
  static class ServletContainerCustomizer implements EmbeddedServletContainerCustomizer {

    @Override
    public void customize(final ConfigurableEmbeddedServletContainer container) {
      ((TomcatEmbeddedServletContainerFactory) container).addContextCustomizers(new TomcatContextCustomizer() {
        @Override
        public void customize(Context context) {
          // Setting HttpOnly to false allows access to cookies from JavaScript. We need this
          // so that the frontend is able to delete the session cookie on logout.
          context.setUseHttpOnly(false);
        }
      });
    }
  }
}