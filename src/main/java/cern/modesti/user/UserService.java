package cern.modesti.user;

import cern.modesti.security.ldap.LdapUserDetailsMapper;
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
public class UserService {

  private static final String BASE = "OU=Users,OU=Organic Units";

  @Autowired
  @Qualifier("anonymousLdapTemplate")
  LdapTemplate ldapTemplate;

  @Autowired
  LdapUserDetailsMapper mapper;

  @Autowired
  Environment env;

  public User findOneByUsername(String username) {
    EqualsFilter filter = new EqualsFilter("CN", username);
    return ldapTemplate.search(LdapQueryBuilder.query().base(BASE).filter(filter), mapper).stream().findFirst().orElse(null);
  }

  public List<User> findByUsernameStartsWith(String query) {
    LikeFilter filter = new LikeFilter("CN", query + "*");
    return ldapTemplate.search(LdapQueryBuilder.query().base(BASE).countLimit(10).filter(filter), mapper);
  }

  public List<User> findByUsernameOrNameAndRole(String query, String[] roles) throws InvalidNameException {
    AndFilter and = new AndFilter();

    OrFilter roleOr = new OrFilter();
    for (String role : roles) {
      LdapName ln = new LdapName(env.getRequiredProperty("ldap.group.filter"));
      ln.add(new Rdn("cn", role));

      // E.g.: (memberOf=CN=modesti-developers,OU=e-groups,OU=Workgroups,DC=cern,DC=ch)
      EqualsFilter filter = new EqualsFilter("memberOf", ln.toString());
      roleOr.or(filter);
    }

    and.and(roleOr);

    OrFilter nameOr = new OrFilter();
    nameOr.or(new LikeFilter("CN", query + "*")).or(new LikeFilter("givenName", query + "*")).or(new LikeFilter("SN", query + "*"));

    and.and(nameOr);

    return ldapTemplate.search(LdapUtils.emptyLdapName(), and.encode(), SearchControls.SUBTREE_SCOPE, null, mapper);
  }
}
