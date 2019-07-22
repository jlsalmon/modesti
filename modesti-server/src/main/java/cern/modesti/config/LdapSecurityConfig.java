package cern.modesti.config;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
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
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

import cern.modesti.security.ldap.LdapUserDetailsMapper;
import cern.modesti.security.ldap.RecursiveLdapAuthoritiesPopulator;
import cern.modesti.security.mock.MockAuthenticationProvider;
import cern.modesti.security.mock.MockUserService;
import cern.modesti.security.mock.MockUserServiceImpl;

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
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
public class LdapSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  Environment env;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .requestMatcher(new BasicRequestMatcher())
        // Authentication is required for all API endpoints
        .authorizeRequests()
        .antMatchers("/", "/api/plugins", "/api/user", "/login", "/api/ldap_login", "/api/is_tn_address").permitAll()
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
  public LdapUserDetailsService ldapUserDetailsService() {
    return new LdapUserDetailsService(ldapUserSearch(), ldapAuthoritiesPopulator());
  }
  
  @Bean
  public LdapUserSearch ldapUserSearch() {
    return new FilterBasedLdapUserSearch(
        env.getRequiredProperty("ldap.user.base"), 
        env.getRequiredProperty("ldap.group.filter"),
        anonymousContextSource());
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
  static class ServletContainerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
      // Setting HttpOnly to false allows access to cookies from JavaScript. We need this
      // so that the frontend is able to delete the session cookie on logout.
      factory.addContextCustomizers(context -> context.setUseHttpOnly(false));
    }
  }
}
