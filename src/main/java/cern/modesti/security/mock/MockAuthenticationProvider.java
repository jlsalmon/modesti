package cern.modesti.security.mock;

import cern.modesti.user.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

/**
 * An {@link AuthenticationProvider} that delegates to a {@link MockUserService}
 * to provide dummy authentication for development.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Profile("dev")
public class MockAuthenticationProvider implements AuthenticationProvider {

  MockUserService userService;

  public MockAuthenticationProvider() {}

  public MockAuthenticationProvider(MockUserService userService) {
    this.userService = userService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    User user = userService.findOneByUsername(authentication.getName());
    if (user == null) {
      throw new UsernameNotFoundException(format("User %s not found", authentication.getName()));
    }

    return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return true;
  }
}
