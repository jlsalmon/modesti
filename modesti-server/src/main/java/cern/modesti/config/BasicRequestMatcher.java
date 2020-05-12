package cern.modesti.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Matches requests with basic authentication
 * 
 * @author Ivan Prieto Barreiro
 */
public class BasicRequestMatcher implements RequestMatcher {

  @Override
  public boolean matches(HttpServletRequest request) {
    String auth = request.getHeader("Authorization");
    return auth != null && auth.startsWith("Basic");
  }
}
