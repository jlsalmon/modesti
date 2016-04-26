package cern.modesti.security.ldap;

import cern.modesti.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Justin Lewis Salmon
 */
@Service
public class LdapUserDetailsService implements UserDetailsService {

  @Autowired
  private UserService userService;

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userService.findOneByUsername(username);
  }
}
