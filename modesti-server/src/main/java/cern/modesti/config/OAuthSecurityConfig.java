package cern.modesti.config;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuration class for OAuth authentication
 * 
 * @author Ivan Prieto Barreiro
 */
@EnableOAuth2Sso
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RestController
@Order(2)
@Profile("!dev")
public class OAuthSecurityConfig extends WebSecurityConfigurerAdapter {

  /**
   * REST interface providing the user information.
   * In a real application you should only expose the absolute necessary and not the entire {@link Principal} object
   * @param principal The User information
   * @return The user information
   */
  @GetMapping("/api/user")
  public Principal user(Principal principal) {
    return principal;
  }

  /**
   * @param response Response redirects back to the frontend page.
   * @param callback The URL of frontend to which backend will redirect after successful log in.
   * @throws IOException sendRedirect failed.
   */
  @GetMapping("/api/sso")
  public void sso(
    HttpServletResponse response,
    @RequestParam("callback") String callback
  ) throws IOException {
    response.sendRedirect(callback);
  }
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http
      .csrf().disable()
      .authorizeRequests()
      .antMatchers("/", "/api/plugins", "/api/user", "/login", "/api/ldap_login", "/api/is_tn_address").permitAll()
      .antMatchers("/api/**").authenticated()
      .and().logout().logoutSuccessUrl("/").deleteCookies("JSESSIONID").invalidateHttpSession(true).clearAuthentication(true)
      .permitAll()
      ;
    // @formatter:on
  }
    
  /**
   * Bean for extracting the user authorities
   * @return
   */
  @Bean
  public AuthoritiesExtractor oauthAuthoritiesExtractor() {
      return new FixedAuthoritiesExtractor();
  }
    
  /**
   * @return REST template that is able to make OAuth2-authenticated REST requests with the credentials of the provided resource.
   */
  @Bean
  public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
    return new OAuth2RestTemplate(resource, context);
  }
}
