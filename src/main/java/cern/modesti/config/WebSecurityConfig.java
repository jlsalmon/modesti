/**
 *
 */
package cern.modesti.config;

import cern.modesti.security.ldap.LdapUserManager;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.spring.SpringProcessEngineConfiguration;
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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;


/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableWebMvcSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
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

//  @Configuration
//  @Profile("test")
//  protected static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {
//
//    @Override
//    public void init(AuthenticationManagerBuilder auth) throws Exception {
//      auth
//      .ldapAuthentication()
//      .userDnPatterns("uid={0},ou=people")
//      .groupSearchBase("ou=groups")
//      .contextSource().ldif("classpath:test-server.ldif");
//    }
//  }

  @Bean
  public LdapAuthenticationProvider ldapAuthenticationProvider() {
    return new LdapAuthenticationProvider(ldapAuthenticator());
  }

  @Bean
  public LdapAuthenticator ldapAuthenticator() {
    BindAuthenticator authenticator = new BindAuthenticator(contextSource());
    String[] userDnPatterns = new String[]{env.getRequiredProperty("ldap.user")};
    authenticator.setUserDnPatterns(userDnPatterns);
    return authenticator;
  }

  @Bean
  public LdapTemplate ldapTemplate() {
    return new LdapTemplate(contextSource());
  }

  @Bean
  public LdapContextSource contextSource() {
    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(env.getRequiredProperty("ldap.auth.url"));
    contextSource.setBase(env.getRequiredProperty("ldap.base"));
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