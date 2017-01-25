package cern.modesti.security.ldap;

import cern.modesti.user.User;
import cern.modesti.security.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.naming.InvalidNameException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.List;

/**
 * A {@link UserService} implementation that performs searches against an LDAP
 * server.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Profile({"test", "prod"})
@Slf4j
public class LdapUserService implements UserService {

  private final LdapTemplate ldapTemplate;

  private final LdapUserDetailsMapper mapper;

  private final Environment environment;

  private final String ldapUserBase;

  private final String ldapGroupFilter;

  @Autowired
  public LdapUserService(@Qualifier("anonymousLdapTemplate") LdapTemplate ldapTemplate, LdapUserDetailsMapper mapper, Environment environment) {
    this.ldapTemplate = ldapTemplate;
    this.mapper = mapper;
    this.environment = environment;
    this.ldapUserBase = environment.getRequiredProperty("ldap.user.base");
    this.ldapGroupFilter = environment.getRequiredProperty("ldap.group.filter");
  }

  @Override
  public User findOneByUsername(String username) {
    EqualsFilter filter = new EqualsFilter("CN", username);
    return ldapTemplate.search(LdapQueryBuilder.query().base(ldapUserBase).filter(filter), mapper)
        .stream().findFirst().orElse(null);
  }

  @Override
  public List<User> findByUsernameStartsWith(String query) {
    LikeFilter filter = new LikeFilter("CN", query + "*");
    return ldapTemplate.search(LdapQueryBuilder.query().base(ldapUserBase).countLimit(10).filter(filter), mapper);
  }

  @Override
  public List<User> findByNameAndGroup(String query, List<String> groups) {
    AndFilter and = new AndFilter();

    OrFilter roleOr;
    try {
      roleOr = memberOf(groups);
    } catch (InvalidNameException e) {
      throw new RuntimeException("Error querying LDAP server", e);
    }

    and.and(roleOr);

    OrFilter nameOr = new OrFilter();
    nameOr.or(new LikeFilter("CN", query + "*")).or(new LikeFilter("givenName", query + "*")).or(new LikeFilter("SN", query + "*"));

    and.and(nameOr);

    try {
      return ldapTemplate.search(ldapUserBase, and.encode(), SearchControls.SUBTREE_SCOPE, null, mapper);
    } catch (Exception e) {

      // The embedded LDAP server (apacheds-1.5.5) doesn't currently support searching using memberOf. So in this case we just ignore
      // the group search and do only a name-based search.
      if (environment.acceptsProfiles("dev")) {
        log.warn("Failed to search users using 'memberOf'. Falling back to name-only search");
        return ldapTemplate.search(ldapUserBase, nameOr.encode(), SearchControls.SUBTREE_SCOPE, null, mapper);
      } else {
        throw e;
      }
    }
  }

  @Override
  public List<String> findGroupsByName(String query) {
    return null;
  }

  @Override
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      log.warn("Current user is null!");
      return null;
    }
    return (User) authentication.getPrincipal();
  }

  public List<User> findByGroup(List<String> groups) {
    OrFilter filter;
    try {
      filter = memberOf(groups);
    } catch (InvalidNameException e) {
      throw new RuntimeException("Error querying LDAP server", e);
    }

    return ldapTemplate.search(ldapUserBase, filter.encode(), SearchControls.SUBTREE_SCOPE, null, mapper);
  }

  private OrFilter memberOf(List<String> groups) throws InvalidNameException {
    OrFilter or = new OrFilter();
    for (String group : groups) {
      LdapName ln = new LdapName(ldapGroupFilter);
      ln.add(new Rdn("cn", group));

      // The magic number will trigger a recursive search of nested groups. It's slow, but it works.
      // See https://msdn.microsoft.com/en-us/library/aa746475(VS.85).aspx
      EqualsFilter filter = new EqualsFilter("memberOf:1.2.840.113556.1.4.1941:", ln.toString());
      or.or(filter);
    }
    return or;
  }
}
