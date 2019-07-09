package cern.modesti.security.oauth;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import cern.modesti.security.ldap.RecursiveLdapAuthoritiesPopulator;
import cern.modesti.user.UserImpl;

/**
 * Extracts the principal from the OAuth authentication
 * 
 * @author Ivan Prieto Barreiro
 */
@Component
public class OauthPrincipalExtractor implements PrincipalExtractor {

  @Autowired
  private RecursiveLdapAuthoritiesPopulator ldapAuthoritiesPopulator;
  
  @Override
  public Object extractPrincipal(Map<String, Object> map) {
    UserImpl user = new UserImpl();
    user.setEmployeeId((Integer) map.get("personid"));
    user.setUsername((String) map.get("username"));
    user.setFirstName((String) map.get("first_name"));
    user.setLastName((String) map.get("last_name"));
    user.setEmail((String) map.get("email"));
    Set<GrantedAuthority> authorities = ldapAuthoritiesPopulator.getGroupMembershipRoles(null, user.getUsername());
    user.setAuthorities(new ArrayList<>(authorities));
    return user;
  }
}
