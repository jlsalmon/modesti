/**
 *
 */
package cern.modesti.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

/**
 * @author Justin Lewis Salmon
 *
 */
@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http
//      .authorizeRequests()
//        .antMatchers("/css/**").permitAll()
//        .anyRequest().fullyAuthenticated()
//        .and()
//      .formLogin();
//  }
//
//  @Configuration
//  protected static class AuthenticationConfiguration extends
//      GlobalAuthenticationConfigurerAdapter {
//
//    @Override
//    public void init(AuthenticationManagerBuilder auth) throws Exception {
//      auth
//        .ldapAuthentication()
//          .userDnPatterns("uid={0},ou=people")
//          .groupSearchBase("ou=groups");
//          //.contextSource().ldif("classpath:test-server.ldif");
//    }
//  }

  @Autowired
  Environment env;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
       /*
        * Set up your spring security config here. For example...
       */
//       http.authorizeRequests().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll();
      http
      .authorizeRequests()
        .antMatchers("/css/**").permitAll()
        .anyRequest().fullyAuthenticated()
        .and()
      .formLogin();
       /*
        * Use HTTPs for ALL requests
       */
       //http.requiresChannel().anyRequest().requiresSecure();
       //http.portMapper().http(8080).mapsTo(env.getRequiredProperty("server.port", Integer.class));
  }

  @Override
  protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
       authManagerBuilder.authenticationProvider(activeDirectoryLdapAuthenticationProvider()).userDetailsService(userDetailsService());
  }

  @Override
  @Bean
  public AuthenticationManager authenticationManager() {
       return new ProviderManager(Arrays.asList(activeDirectoryLdapAuthenticationProvider()));
  }
  @Bean
  public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
       ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(env.getRequiredProperty("ldap.base"), env.getRequiredProperty("ldap.url"));
       provider.setConvertSubErrorCodesToExceptions(true);
       provider.setUseAuthenticationRequestCredentials(true);
       return provider;
  }

//  @Bean
//  public LdapContextSource contextSource () {
//      LdapContextSource contextSource= new LdapContextSource();
//      contextSource.setUrl(env.getRequiredProperty("ldap.url"));
//      contextSource.setBase(env.getRequiredProperty("ldap.base"));
////      contextSource.setUserDn(env.getRequiredProperty("ldap.user"));
////      contextSource.setPassword(env.getRequiredProperty("ldap.password"));
//      return contextSource;
//  }
//
//  @Bean
//  public LdapTemplate ldapTemplate() {
//      return new LdapTemplate(contextSource());
//  }
}