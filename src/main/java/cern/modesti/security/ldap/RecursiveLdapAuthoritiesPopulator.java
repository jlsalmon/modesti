package cern.modesti.security.ldap;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom {@link org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator}
 * to make sure that nested groups are retrieved recursively at login, up to a
 * certain maximum depth.
 *
 * @author Justin Lewis Salmon
 */
public class RecursiveLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

  private static final int MAX_RECURSION_DEPTH = 5;

  String base;
  String userBase;

  public RecursiveLdapAuthoritiesPopulator(ContextSource contextSource, String base, String userBase, String groupSearchBase, String groupSearchFilter) {
    super(contextSource, groupSearchBase);
    this.base = base;
    this.userBase = userBase;
    setGroupSearchFilter(groupSearchFilter);
  }

  @Override
  public Set<GrantedAuthority> getGroupMembershipRoles(String userDn, String username) {
    List<String> groups = getLdapTemplate().search(LdapQueryBuilder.query().base(getGroupSearchBase())
            .filter("member=CN=" + username + "," + userBase + "," + base),
        (AttributesMapper<String>) attributes -> (String) attributes.get("cn").get());

    List<String> nestedGroups = new ArrayList<>();

    for (String group : groups) {
      nestedGroups.addAll(getNestedGroups(group, MAX_RECURSION_DEPTH));
    }

    groups.addAll(nestedGroups);
    return groups.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
  }

  /**
   * Performs the nested group search
   *
   * @param group the group to search for
   * @param depth the depth remaining, when 0 recursion will end
   *
   */
  private List<String> getNestedGroups(String group, int depth) {
    if (depth == 0) {
      return Collections.emptyList();
    }

    List<String> groups = getLdapTemplate().search(LdapQueryBuilder.query().base(getGroupSearchBase())
            .filter("member=CN=" + group + "," + getGroupSearchFilter()),
        (AttributesMapper<String>) attributes -> (String) attributes.get("cn").get());

    List<String> nestedGroups = new ArrayList<>();

    for (String dn : groups) {
      nestedGroups.addAll(getNestedGroups(dn, depth - 1));
    }

    groups.addAll(nestedGroups);
    return groups;
  }
}
