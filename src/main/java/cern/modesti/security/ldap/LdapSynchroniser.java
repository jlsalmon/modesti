package cern.modesti.security.ldap;

import cern.modesti.user.Role;
import cern.modesti.user.User;
import cern.modesti.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.*;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Service
@EnableScheduling
@Slf4j
@Profile({"test", "prod"})
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
  public void synchroniseUsersAndGroups() throws InvalidNameException {
    log.debug("synchronising users and groups with LDAP server");
    Set<String> groupIds = new HashSet<>();

    // TODO: remove this TIM specific code
    groupIds.addAll(env.getRequiredProperty("modesti.role.creators", List.class));
    groupIds.addAll(env.getRequiredProperty("modesti.role.approvers", List.class));
    groupIds.addAll(env.getRequiredProperty("modesti.role.cablers", List.class));
    groupIds.addAll(env.getRequiredProperty("modesti.role.administrators", List.class));

    for (String groupId : groupIds) {
      synchroniseGroup(groupId);
    }
  }

  private void synchroniseGroup(String groupId) throws InvalidNameException {
    LdapName ln = new LdapName(env.getRequiredProperty("ldap.group.filter"));
    ln.add(new Rdn("cn", groupId));

    // E.g.: (memberOf=CN=modesti-developers,OU=e-groups,OU=Workgroups,DC=cern,DC=ch)
    EqualsFilter filter = new EqualsFilter("memberOf", ln.toString());
    List users = ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), SearchControls.SUBTREE_SCOPE, null, (Object ctx) -> ctx);

    for (Object object : users) {
      DirContextAdapter adapter = (DirContextAdapter) object;

      if (adapter.attributeExists("employeeID")) {
        String username = adapter.getStringAttribute("CN");
        synchroniseUser(username, groupId, adapter);
      }

      // Recursively synchronise groups-in-groups
      else if (adapter.attributeExists("member")) {
        synchroniseGroup(adapter.getStringAttribute("CN"));
      }
    }
  }

  private void synchroniseUser(String username, String groupId, DirContextAdapter adapter) {
    User user = userRepository.findOneByUsername(username);
    if (user == null) {
      user = new User();
      user.setEmployeeId(Integer.valueOf(adapter.getStringAttribute("employeeID")));
      user.setUsername(username);
      user.setFirstName(adapter.getStringAttribute("givenName"));
      user.setLastName(adapter.getStringAttribute("sn"));
      user.setEmail(adapter.getStringAttribute("mail"));
      user.setAuthorities(new ArrayList<>(Collections.singleton(new Role(groupId))));
      log.debug(format("adding new user %s", user));

    } else {
      Role role = new Role(groupId);

      if (!user.getAuthorities().contains(role)) {
        log.debug(format("adding user %s to group '%s'", user, groupId));
        user.getAuthorities().add(role);
      }
    }

    userRepository.save(user);
  }
}
