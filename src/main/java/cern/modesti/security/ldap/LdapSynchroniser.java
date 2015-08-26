package cern.modesti.security.ldap;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchControls;
import java.util.*;

import static java.lang.String.format;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@EnableScheduling
@Slf4j
@Profile({"dev", "prod"})
public class LdapSynchroniser {

  @Autowired
  Environment env;

  @Autowired
  UserRepository userRepository;

  @Autowired
  @Qualifier("anonymousLdapTemplate")
  LdapTemplate ldapTemplate;

  /**
   * Get all users of the configured groups and add them. Runs once per hour
   */
  @Scheduled(cron = "0 0 * * * *")
  public void synchroniseUsersAndGroups() {
    log.debug("synchronising users and groups with LDAP server");
    Set<String> groupIds = new HashSet<>();

    groupIds.addAll(env.getRequiredProperty("modesti.role.creators", List.class));
    groupIds.addAll(env.getRequiredProperty("modesti.role.approvers", List.class));
    groupIds.addAll(env.getRequiredProperty("modesti.role.cablers", List.class));
    groupIds.addAll(env.getRequiredProperty("modesti.role.administrators", List.class));

    for (String groupId : groupIds) {
      DistinguishedName dn = new DistinguishedName(env.getRequiredProperty("ldap.group.filter"));
      dn.append("cn", groupId);

      // E.g.: (memberOf=CN=modesti-developers,OU=e-groups,OU=Workgroups,DC=cern,DC=ch)
      EqualsFilter filter = new EqualsFilter("memberOf", dn.toString());
      List users = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), SearchControls.SUBTREE_SCOPE, null, (Object ctx) -> ctx);

      for (Object object : users) {
        DirContextAdapter adapter = (DirContextAdapter) object;
        String username = adapter.getStringAttribute("CN");

        User user = userRepository.findOneByUsername(username);

        if (user == null) {
          user = new User();
          user.setUsername(username);
          user.setEmployeeId(Integer.valueOf(adapter.getStringAttribute("employeeID")));
          user.setFirstName(adapter.getStringAttribute("givenName"));
          user.setLastName(adapter.getStringAttribute("sn"));
          user.setEmail(adapter.getStringAttribute("mail"));
          user.setAuthorities(new HashSet<>(Collections.singleton(new Role(groupId))));
          log.debug(format("adding new user %s", user));

        } else {
          Set<Role> authorities = (Set<Role>) user.getAuthorities();
          authorities.add(new Role(groupId));
          user.setAuthorities(authorities);
          log.debug(format("adding user %s to group '%s'", user, groupId));
        }

        userRepository.save(user);
      }
    }
  }
}
