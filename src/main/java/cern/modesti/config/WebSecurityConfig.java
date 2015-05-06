/**
 *
 */
package cern.modesti.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.access.event.LoggerListener;
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
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  Environment env;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authenticationProvider(ldapAuthenticationProvider());

    http.authorizeRequests().antMatchers("/user/**").permitAll().anyRequest().fullyAuthenticated().and().httpBasic();
  }

  //    @Configuration
  //    protected static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {
  //
  //      @Override
  //      public void init(AuthenticationManagerBuilder auth) throws Exception {
  //        auth
  //          .ldapAuthentication()
  //
  //            .userDnPatterns("CN={0},OU=Users,OU=Organic Units")
  //            .groupSearchBase("OU=e-groups,OU=Workgroups")
  //            .contextSource()//.ldif("classpath:test-server.ldif")
  //            .url("ldaps://cerndc.cern.ch:636/DC=cern,DC=ch");
  //      }
  //    }


  @Bean
  public LoggerListener loggerListener() {
    return new LoggerListener();
  }

  @Bean
  public LdapContextSource contextSource() {
    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(env.getRequiredProperty("ldap.url"));
    contextSource.setBase(env.getRequiredProperty("ldap.base"));
    return contextSource;
  }

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
}