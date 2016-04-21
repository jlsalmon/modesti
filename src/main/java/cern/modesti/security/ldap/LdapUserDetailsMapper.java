package cern.modesti.security.ldap;

import cern.modesti.user.Role;
import cern.modesti.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

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
@Slf4j
public class LdapUserDetailsMapper extends AbstractContextMapper<User> implements UserDetailsContextMapper {

  @Override
  public UserDetails mapUserFromContext(DirContextOperations context, String username, Collection<? extends GrantedAuthority> grantedAuthorities) {
    return doMapFromContext(context);
  }

  @Override
  protected User doMapFromContext(DirContextOperations context) {
    User user = new User();
    user.setEmployeeId(Integer.valueOf(context.getStringAttribute("employeeID")));
    user.setUsername(context.getStringAttribute("cn"));
    user.setFirstName(context.getStringAttribute("givenName"));
    user.setLastName(context.getStringAttribute("sn"));
    user.setEmail(context.getStringAttribute("mail"));

    if (context.attributeExists("memberOf")) {
      Set<GrantedAuthority> authorities = new HashSet<>();

      for (Object attr : context.getObjectAttributes("memberOf")) {
        String group = (String) attr;

        if (group.contains("e-groups")) {
          group = group.split(",")[0].split("=")[1];
          authorities.add(new Role(group.toLowerCase()));
        }
      }

      for (GrantedAuthority authority : authorities) {
        user.getAuthorities().add((Role) authority);
      }
    }

    return user;
  }

  @Override
  public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {}
}
