package cern.modesti.config;

import cern.modesti.security.ldap.LdapUserDetailsMapper;
import cern.modesti.user.MockAuthenticationProvider;
import org.apache.catalina.Context;
import org.apache.directory.server.core.authn.AuthenticationInterceptor;
import org.apache.directory.server.core.exception.ExceptionInterceptor;
import org.apache.directory.server.core.interceptor.Interceptor;
import org.apache.directory.server.core.normalization.NormalizationInterceptor;
import org.apache.directory.server.core.operational.OperationalAttributeInterceptor;
import org.apache.directory.server.core.referral.ReferralInterceptor;
import org.apache.directory.server.core.schema.SchemaInterceptor;
import org.apache.directory.server.core.subtree.SubentryInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.server.ApacheDSContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for the core web security and LDAP beans.
 * <p>
 * When running with the {@literal dev} profile, an in-memory LDAP server is used.
 * <p>
 * Otherwise, The authentication/authorisation mechanism uses two different LDAP servers:
 * cerndc.cern.ch and xldap.cern.ch. The former is available in authenticated
 * mode only, from inside CERN only, and is used to authenticate a user. The
 * latter is available anonymously, from inside CERN only, and is used to
 * perform anonymous lookups on the LDAP server in order to do things like
 * lookup all the e-groups a particular user is a member of, or to check if a
 * user is a member of a particular e-group.
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
//    http.authenticationProvider(ldapAuthenticationProvider());

    http
        // Enable basic HTTP authentication
        .httpBasic()
        // Authentication is required for all URLs
        .and().authorizeRequests().anyRequest().authenticated()
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
          .contextSource(contextSource());
    }
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

  @Bean
  public MockAuthenticationProvider mockAuthenticationProvider() {
    return new MockAuthenticationProvider();
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

  // Post process the embedded LDAP server (apacheds) to allow custom schema attributes in the LDIF file
  @Bean
  public static BeanPostProcessor apacheDSContainerConfigurer() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ApacheDSContainer) {
          List<Interceptor> interceptors = new ArrayList<>();
          interceptors.add(new NormalizationInterceptor());
          interceptors.add(new AuthenticationInterceptor());
          interceptors.add(new ReferralInterceptor());
          interceptors.add(new ExceptionInterceptor());
          interceptors.add(new OperationalAttributeInterceptor());
          interceptors.add(new SchemaInterceptor()); // this has been added
          interceptors.add(new SubentryInterceptor());
          ((ApacheDSContainer) bean).getService().setInterceptors(interceptors);
        }
        return bean;
      }

      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
      }
    };
  }
}
