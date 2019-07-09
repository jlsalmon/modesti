package cern.modesti.security;

import cern.modesti.user.UserImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST controller to handle user login with LDAP.
 *
 * @author Justin Lewis Salmon
 */
@RestController
public class LdapLoginController {

  @RequestMapping(value = "/api/ldap_login")
  public UserDetails login(Principal principal) {
    UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
    return (UserImpl) token.getPrincipal();
  }
}
