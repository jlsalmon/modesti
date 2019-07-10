package cern.modesti.security.oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import cern.modesti.security.ldap.RecursiveLdapAuthoritiesPopulator;

/**
 * Extracts the authorities using {@link cern.modesti.security.ldap.RecursiveLdapAuthoritiesPopulator}
 * to search e-groups recursively.
 * 
 * @author Ivan Prieto Barreiro
 */
@Component
public class OauthAuthoritiesExtractor implements AuthoritiesExtractor {

  @Autowired
  private RecursiveLdapAuthoritiesPopulator ldapAuthoritiesPopulator;
  
  @Override
  public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
    String username = (String) map.get("username");
    Set<GrantedAuthority> authorities = ldapAuthoritiesPopulator.getGroupMembershipRoles(null, username);
    return new ArrayList<>(authorities);
  }
}
