package cern.modesti.security.ldap;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Component
public class LdapUserDetailsMapper implements UserDetailsContextMapper {

  @Override
  public UserDetails mapUserFromContext(DirContextOperations context, String username, Collection<? extends GrantedAuthority> authorities) {
    Integer id = Integer.valueOf(context.getStringAttribute("employeeID"));
    String firstName = context.getStringAttribute("givenName");
    String lastName = context.getStringAttribute("sn");
    String email = context.getStringAttribute("mail");

    return new User(id, username, firstName, lastName, email);
  }

  @Override
  public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
  }
}
