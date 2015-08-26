package cern.modesti.security.ldap;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
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
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchControls;
import java.util.*;

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
  IdentityService identityService;

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
      if (identityService.createGroupQuery().groupId(groupId).singleResult() == null) {
        log.debug("adding new group " + groupId);
        Group group = identityService.newGroup(groupId);
        group.setName(groupId);
        identityService.saveGroup(group);
      }

      DistinguishedName dn = new DistinguishedName(env.getRequiredProperty("ldap.group.filter"));
      dn.append("cn", groupId);

      // E.g.: (memberOf=CN=modesti-developers,OU=e-groups,OU=Workgroups,DC=cern,DC=ch)
      EqualsFilter filter = new EqualsFilter("memberOf", dn.toString());
      List users = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), SearchControls.SUBTREE_SCOPE, null, (Object ctx) -> ctx);

      for (Object object : users) {
        DirContextAdapter adapter = (DirContextAdapter) object;
        String userId = adapter.getStringAttribute("CN");

        if (identityService.createUserQuery().userId(userId).singleResult() == null) {
          log.debug("adding new user " + userId);
          User user = identityService.newUser(adapter.getStringAttribute("CN"));
          user.setFirstName(adapter.getStringAttribute("givenName"));
          user.setLastName(adapter.getStringAttribute("sn"));
          user.setEmail(adapter.getStringAttribute("mail"));
          identityService.saveUser(user);
        }

        // Add the user to the group (unless he/she is already a member)
        if (identityService.createUserQuery().userId(userId).memberOfGroup(groupId).singleResult() == null) {
          log.debug("adding user " + userId + " to group " + groupId);
          identityService.createMembership(userId, groupId);
        }
      }
    }
  }
}
