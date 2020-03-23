package cern.modesti.config;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import cern.modesti.security.oauth.*;

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

/**
 * Configuration class for OAuth authentication
 * 
 * @author Ivan Prieto Barreiro
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RestController
@Order(2)
@Profile("!dev")
public class OAuthSecurityConfig {

  /**
   * REST interface providing the user information.
   * In a real application you should only expose the absolute necessary and not the entire {@link Principal} object
   * @param principal The User information
   * @return The user information
   */
  @GetMapping("/api/user")
  public OidcUser getOidcUserPrincipal(@AuthenticationPrincipal OidcUser principal) {
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
  
  /**
   * Configures OAuth Login with Spring Security 5.
   * @return
   */
  @Bean
  public WebSecurityConfigurerAdapter webSecurityConfigurer(
      @Value("${spring.security.oauth2.client.registration.modesti-sso-dev.client-id}") 
      final String registrationId,
      KeycloakOauth2UserService keycloakOidcUserService,
      KeycloakLogoutHandler keycloakLogoutHandler
  ) {
      return new WebSecurityConfigurerAdapter() {
        @Override
        public void configure(HttpSecurity http) throws Exception {
          http
              .csrf().disable()
              .authorizeRequests()
              .antMatchers("/", "/api/plugins", "/api/user", "/login").permitAll()
              .antMatchers("/api/**").authenticated()
              .and().logout().addLogoutHandler(keycloakLogoutHandler).and()
              .oauth2Login().userInfoEndpoint().oidcUserService(keycloakOidcUserService)
              .and().loginPage(DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + registrationId)
            ;
          }
      };
  }  
  
  @Bean
  KeycloakOauth2UserService keycloakOidcUserService(OAuth2ClientProperties oauth2ClientProperties) {
    NimbusJwtDecoderJwkSupport jwtDecoder = new NimbusJwtDecoderJwkSupport(
        oauth2ClientProperties.getProvider().get("keycloak").getJwkSetUri());

    SimpleAuthorityMapper authoritiesMapper = new SimpleAuthorityMapper();
    authoritiesMapper.setConvertToUpperCase(true);

    return new KeycloakOauth2UserService(jwtDecoder, authoritiesMapper);
  }
  
  @Bean
  KeycloakLogoutHandler keycloakLogoutHandler() {
    return new KeycloakLogoutHandler(new RestTemplate());
  }
}
