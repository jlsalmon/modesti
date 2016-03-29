package cern.modesti.config;

import cern.modesti.user.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.ArrayList;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableWebSecurity
@Order(2)
@Profile("dev")
public class WebSecurityConfigDev extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        // Enable basic HTTP authentication
        .httpBasic()
        // Authentication is required for all URLs
        .and().authorizeRequests().anyRequest().authenticated()
        .and().csrf().disable();
  }

  @Configuration
  protected static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {

    static User TEST_USER = new User("1", "test", 1234, "Test", "User", "test.user@example.com", new ArrayList<>());

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(new AuthenticationProvider() {
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
          return new UsernamePasswordAuthenticationToken(TEST_USER, null);
        }

        @Override
        public boolean supports(Class<?> authentication) {
          return true;
        }
      });
    }
  }
}
