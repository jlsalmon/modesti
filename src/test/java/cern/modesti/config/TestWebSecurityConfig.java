/**
 *
 */
package cern.modesti.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableWebMvcSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
@Profile("test")
public class TestWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
        .antMatchers("/css/**").permitAll()
        .anyRequest().fullyAuthenticated()
        .and()
      .formLogin();
  }

  @Configuration
  protected static class AuthenticationConfiguration extends
      GlobalAuthenticationConfigurerAdapter {

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
      auth
        .ldapAuthentication()
          .userDnPatterns("uid={0},ou=people")
          .groupSearchBase("ou=groups")
          .contextSource().ldif("classpath:test-server.ldif");
    }
  }
}
