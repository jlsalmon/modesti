package cern.modesti.user;

import cern.modesti.security.ldap.LdapUserDetailsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.repository.LdapRepository;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class UserService {

  @Autowired
  @Qualifier("anonymousLdapTemplate")
  LdapTemplate ldapTemplate;

  @Autowired
  LdapUserDetailsMapper mapper;

  @Autowired
  Environment env;

  public User findOneByUsername(String username) {
    EqualsFilter filter = new EqualsFilter("CN", username);
    return ldapTemplate.search(LdapQueryBuilder.query().base(env.getRequiredProperty("ldap.user.base")).filter(filter), mapper)
        .stream().findFirst().orElse(null);
  }

  public List<User> findByUsernameStartsWith(String query) {
    LikeFilter filter = new LikeFilter("CN", query + "*");
    return ldapTemplate.search(LdapQueryBuilder.query().base(env.getRequiredProperty("ldap.user.base")).countLimit(10).filter(filter), mapper);
  }

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
      return ldapTemplate.search(env.getRequiredProperty("ldap.user.base"), and.encode(), SearchControls.SUBTREE_SCOPE, null, mapper);
    } catch (Exception e) {

      // The embedded LDAP server (apacheds-1.5.5) doesn't currently support searching using memberOf. So in this case we just ignore
      // the group search and do only a name-based search.
      if (env.acceptsProfiles("dev")) {
        log.warn("Failed to search users using 'memberOf'. Falling back to name-only search");
        return ldapTemplate.search(env.getRequiredProperty("ldap.user.base"), nameOr.encode(), SearchControls.SUBTREE_SCOPE, null, mapper);
      } else {
        throw e;
      }
    }
  }

  public List<User> findByGroup(List<String> groups) {
    OrFilter filter;
    try {
      filter = memberOf(groups);
    } catch (InvalidNameException e) {
      throw new RuntimeException("Error querying LDAP server", e);
    }

    return ldapTemplate.search(env.getRequiredProperty("ldap.user.base"), filter.encode(), SearchControls.SUBTREE_SCOPE, null, mapper);
  }

  private OrFilter memberOf(List<String> groups) throws InvalidNameException {
    OrFilter or = new OrFilter();
    for (String group : groups) {
      LdapName ln = new LdapName(env.getRequiredProperty("ldap.group.filter"));
      ln.add(new Rdn("cn", group));

      // The magic number will trigger a recursive search of nested groups. It's slow, but it works.
      // See https://msdn.microsoft.com/en-us/library/aa746475(VS.85).aspx
      EqualsFilter filter = new EqualsFilter("memberOf:1.2.840.113556.1.4.1941:", ln.toString());
      or.or(filter);
    }
    return or;
  }
}
