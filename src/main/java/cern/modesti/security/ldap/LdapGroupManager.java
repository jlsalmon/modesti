/**
 *
 */
package cern.modesti.security.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

/**
 * @author Justin Lewis Salmon
 *
 */
public class LdapGroupManager extends AbstractManager implements GroupIdentityManager {
  static final Logger LOG = LoggerFactory.getLogger(LdapGroupManager.class);

  @Autowired
  Environment env;

  LdapTemplate ldapTemplate;

  /**
   * @param ldapTemplate
   */
  @Autowired
  public LdapGroupManager(LdapTemplate ldapTemplate) {
    this.ldapTemplate = ldapTemplate;
  }

  @Override
  public Group createNewGroup(String groupId) {
    throw new ActivitiException("LDAP group manager doesn't support creating a new group");
  }

  @Override
  public void insertGroup(Group group) {
    throw new ActivitiException("LDAP group manager doesn't support inserting a group");
  }

  @Override
  public void updateGroup(Group updatedGroup) {
    throw new ActivitiException("LDAP group manager doesn't support updating a group");
  }

  @Override
  public boolean isNewGroup(Group group) {
    throw new ActivitiException("LDAP group manager doesn't support inserting or updating a group");
  }

  @Override
  public void deleteGroup(String groupId) {
    throw new ActivitiException("LDAP group manager doesn't support deleting a group");
  }

  @Override
  public GroupQuery createNewGroupQuery() {
    return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutor());
  }

  @Override
  public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
    // Only support for groupMember() at the moment
    if (query.getUserId() != null) {
      return findGroupsByUser(query.getUserId());
    } else {
      throw new ActivitiIllegalArgumentException("This query is not supported by the LDAPGroupManager");
    }
  }

  @Override
  public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
    return findGroupByQueryCriteria(query, null).size(); // Is there a generic way to do a count(*) in ldap?
  }

  @Override
  public List<Group> findGroupsByUser(final String userId) {
    LOG.debug("findGroupsByUser()");

    String base = "";
    String userFilter = "(cn={0})";
    String groupFilter = "(memberOf:1.2.840.113556.1.4.1941:={0})";

    FilterBasedLdapUserSearch search = new FilterBasedLdapUserSearch(base, userFilter, (BaseLdapPathContextSource) ldapTemplate.getContextSource());
    DirContextOperations operations = search.searchForUser(userId);

    ((DefaultSpringSecurityContextSource) ldapTemplate.getContextSource()).setBase("");

    DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(ldapTemplate.getContextSource(), "OU=Workgroups");
    populator.setGroupSearchFilter(groupFilter);
    Collection<GrantedAuthority> authorities = populator.getGrantedAuthorities(operations, userId);

    List<Group> groups = new ArrayList<>();
    for (GrantedAuthority authority : authorities) {
      Group group = new GroupEntity(authority.getAuthority());
      groups.add(group);
    }
    return groups;

//    // Do the search against Ldap
//    LDAPTemplate ldapTemplate = new LDAPTemplate(ldapConfigurator);
//    return ldapTemplate.execute(new LDAPCallBack<List<Group>>() {
//
//      public List<Group> executeInContext(InitialDirContext initialDirContext) {
//
//        String searchExpression = ldapConfigurator.getLdapQueryBuilder().buildQueryGroupsForUser(ldapConfigurator, userId);
//
//        List<Group> groups = new ArrayList<Group>();
//        try {
//          String baseDn = ldapConfigurator.getGroupBaseDn() != null ? ldapConfigurator.getGroupBaseDn() : ldapConfigurator.getBaseDn();
//          NamingEnumeration< ? > namingEnum = initialDirContext.search(baseDn, searchExpression, createSearchControls());
//          while (namingEnum.hasMore()) { // Should be only one
//            SearchResult result = (SearchResult) namingEnum.next();
//
//            GroupEntity group = new GroupEntity();
//            if (ldapConfigurator.getGroupIdAttribute() != null) {
//              group.setId(result.getAttributes().get(ldapConfigurator.getGroupIdAttribute()).get().toString());
//            }
//            if (ldapConfigurator.getGroupNameAttribute() != null) {
//              group.setName(result.getAttributes().get(ldapConfigurator.getGroupNameAttribute()).get().toString());
//            }
//            if (ldapConfigurator.getGroupTypeAttribute() != null) {
//              group.setType(result.getAttributes().get(ldapConfigurator.getGroupTypeAttribute()).get().toString());
//            }
//            groups.add(group);
//          }
//
//          namingEnum.close();
//
//          // Cache results for later
//          if (ldapGroupCache != null) {
//            ldapGroupCache.add(userId, groups);
//          }
//
//          return groups;
//
//        } catch (NamingException e) {
//          throw new ActivitiException("Could not find groups for user " + userId, e);
//        }
//      }
//
//    });
  }

  @Override
  public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    throw new ActivitiException("LDAP group manager doesn't support querying");
  }

  @Override
  public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
    throw new ActivitiException("LDAP group manager doesn't support querying");
  }
}
