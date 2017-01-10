package cern.modesti.security;

import cern.modesti.user.UserImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST controller to handle user login.
 *
 * @author Justin Lewis Salmon
 */
@RestController
public class LoginController {

  @RequestMapping(value = "/api/login")
  public UserDetails login(Principal principal) {
    UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
    return (UserImpl) token.getPrincipal();
  }
}
