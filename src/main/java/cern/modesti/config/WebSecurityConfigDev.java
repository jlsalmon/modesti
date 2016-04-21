package cern.modesti.config;

import cern.modesti.user.User;
import cern.modesti.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

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

  @Bean
  public LdapTemplate anonymousLdapTemplate() {
    return new LdapTemplate(contextSource());
  }

  public LdapContextSource contextSource() {
    return new LdapContextSource();
  }

  @Configuration
  protected static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {

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
