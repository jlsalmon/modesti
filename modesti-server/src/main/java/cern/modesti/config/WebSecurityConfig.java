package cern.modesti.config;

import cern.modesti.security.ldap.LdapUserDetailsMapper;
import cern.modesti.security.ldap.RecursiveLdapAuthoritiesPopulator;
import cern.modesti.security.mock.MockAuthenticationProvider;
import cern.modesti.security.mock.MockUserService;
import cern.modesti.security.mock.MockUserServiceImpl;
import org.apache.catalina.Context;
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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuration class for the core web security and LDAP beans.
 * <p>
 * When running with the {@literal dev} profile, a mock authentication
 * implementation is used.
 * <p>
 * Otherwise, The authentication/authorisation mechanism uses two different
 * LDAP servers:
 * <ul>
 *   <li>cerndc.cern.ch: used for authentication.</li>
 *   <li>xldap.cern.ch: used to perform anonymous lookups on the LDAP server
 *   in order to do things like lookup all the e-groups a particular user is
 *   a member of, or to check if a user is a member of a particular e-group.
 *   </li>
 * </ul>
 * <p>
 * See <a href=
 * "https://espace.cern.ch/identitymanagement/Wiki%20Pages/Active%20Directory%20Publication.aspx"
 * >this page</a> for documentation about the CERN LDAP structure.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
//@EnableLdapRepositories(basePackageClasses = UserRepository.class, ldapTemplateRef = "anonymousLdapTemplate")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  Environment env;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        // Authentication is required for all API endpoints
        .authorizeRequests()
        .antMatchers("/api/plugins").permitAll()
        .antMatchers("/api/**").authenticated()

        // Enable basic HTTP authentication
        .and().httpBasic().authenticationEntryPoint((request, response, authException) -> {
          String requestedWith = request.getHeader("X-Requested-With");
          if (requestedWith == null || requestedWith.isEmpty()) {
            response.addHeader("WWW-Authenticate", "Basic realm=NICE");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
          } else {
            response.addHeader("WWW-Authenticate", "Application driven");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
          }
        })

        // Enable /logout endpoint
        .and().logout()

        // TODO: implement CSRF protection. Here we just turn it off.
        .and().csrf().disable();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {

    if (env.acceptsProfiles("dev")) {
      auth.authenticationProvider(mockAuthenticationProvider());
    }

    else {
      auth.authenticationProvider(ldapAuthenticationProvider())
          .ldapAuthentication()
          .userDnPatterns(env.getRequiredProperty("ldap.user.filter"))
          .groupSearchBase(env.getRequiredProperty("ldap.group.base"))
          .groupSearchFilter(env.getRequiredProperty("ldap.group.filter"))
          .contextSource(contextSource());
    }
  }

  @Bean
  public LdapAuthenticationProvider ldapAuthenticationProvider() {
    LdapAuthenticationProvider provider = new LdapAuthenticationProvider(ldapAuthenticator(), ldapAuthoritiesPopulator());
    provider.setUserDetailsContextMapper(ldapUserDetailsMapper());
    return provider;
  }

  @Bean
  public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
    return new RecursiveLdapAuthoritiesPopulator(anonymousContextSource(),
        env.getRequiredProperty("ldap.base"), env.getRequiredProperty("ldap.user.base"),
        env.getRequiredProperty("ldap.group.base"), env.getRequiredProperty("ldap.group.filter"));
  }

  @Bean
  public LdapUserDetailsMapper ldapUserDetailsMapper() {
    return new LdapUserDetailsMapper();
  }

  @Bean
  public LdapAuthenticator ldapAuthenticator() {
    BindAuthenticator authenticator = new BindAuthenticator(contextSource());
    String[] userDnPatterns = new String[]{env.getRequiredProperty("ldap.user.filter")};
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

//  @Bean
//  public LoggerListener loggerListener() {
//    return new LoggerListener();
//  }

  @Bean
  @Profile("dev")
  public MockUserService mockUserService() throws IOException {
    return new MockUserServiceImpl();
  }

  @Bean
  @Profile("dev")
  public MockAuthenticationProvider mockAuthenticationProvider() throws IOException {
    return new MockAuthenticationProvider(mockUserService());
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
