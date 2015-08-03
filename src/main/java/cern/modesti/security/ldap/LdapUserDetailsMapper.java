package cern.modesti.security.ldap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for mapping the attributes of an LDAP user object
 * into a MODESTI user object. We store the CERN ID, username, full name and
 * email address of each user.
 *
 * See <a
 * href="https://espace.cern.ch/identitymanagement/Wiki%20Pages/xldap.aspx">the
 * documentation</a> for explanations of the CERN LDAP structure and the user
 * object attributes.
 *
 * @author Justin Lewis Salmon
 */
@Component
public class LdapUserDetailsMapper implements UserDetailsContextMapper {

  @Autowired
  private Environment env;

  @Override
  public UserDetails mapUserFromContext(DirContextOperations context, String username, Collection<? extends GrantedAuthority> grantedAuthorities) {
    Integer id = Integer.valueOf(context.getStringAttribute("employeeID"));
    String firstName = context.getStringAttribute("givenName");
    String lastName = context.getStringAttribute("sn");
    String email = context.getStringAttribute("mail");

    String creators = env.getRequiredProperty("modesti.role.creators", String.class);
    String approvers = env.getRequiredProperty("modesti.role.approvers", String.class);
    String cablers = env.getRequiredProperty("modesti.role.cablers", String.class);
    String administrators = env.getRequiredProperty("modesti.role.administrators", String.class);

    Set<GrantedAuthority> authorities = new HashSet<>();
    authorities.addAll(grantedAuthorities);

    for (Object group : context.getObjectAttributes("memberOf")) {

      if (group.toString().toLowerCase().contains(creators.toLowerCase()) == true) {
        authorities.add(new SimpleGrantedAuthority("ROLE_CREATOR"));
      }
      if (group.toString().toLowerCase().contains(approvers.toLowerCase()) == true) {
        authorities.add(new SimpleGrantedAuthority("ROLE_APPROVER"));
      }
      if (group.toString().toLowerCase().contains(cablers.toLowerCase()) == true) {
        authorities.add(new SimpleGrantedAuthority("ROLE_CABLER"));
      }
      if (group.toString().toLowerCase().contains(administrators.toLowerCase()) == true) {
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
      }
    }

    return new User(id, username, firstName, lastName, email, authorities);
  }

  @Override
  public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
  }
}
