package cern.modesti.security.ldap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cern.modesti.user.Role;
import cern.modesti.user.User;
import cern.modesti.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
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

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails mapUserFromContext(DirContextOperations context, String username, Collection<? extends GrantedAuthority> grantedAuthorities) {
    User user = new User();
    user.setUsername(username);
    user.setEmployeeId(Integer.valueOf(context.getStringAttribute("employeeID")));
    user.setFirstName(context.getStringAttribute("givenName"));
    user.setLastName(context.getStringAttribute("sn"));
    user.setEmail(context.getStringAttribute("mail"));

    String creators = env.getRequiredProperty("modesti.role.creators", String.class);
    String approvers = env.getRequiredProperty("modesti.role.approvers", String.class);
    String cablers = env.getRequiredProperty("modesti.role.cablers", String.class);
    String administrators = env.getRequiredProperty("modesti.role.administrators", String.class);

    Set<GrantedAuthority> authorities = new HashSet<>();
    authorities.addAll(grantedAuthorities);

    for (Object group : context.getObjectAttributes("memberOf")) {

      if (group.toString().toLowerCase().contains(creators.toLowerCase())) {
        authorities.add(new Role(creators));
      }
      if (group.toString().toLowerCase().contains(approvers.toLowerCase())) {
        authorities.add(new Role(approvers));
      }
      if (group.toString().toLowerCase().contains(cablers.toLowerCase())) {
        authorities.add(new Role(cablers));
      }
      if (group.toString().toLowerCase().contains(administrators.toLowerCase())) {
        authorities.add(new Role(administrators));
      }
    }

    user.setAuthorities(authorities);

    // Add this user to the database if they haven't logged in before.
    if (userRepository.findOneByUsername(username) == null) {
      userRepository.save(user);
    }

    return user;
  }

  @Override
  public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
  }
}
