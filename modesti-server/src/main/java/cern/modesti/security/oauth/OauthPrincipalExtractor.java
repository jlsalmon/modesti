package cern.modesti.security.oauth;

import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.stereotype.Component;

import cern.modesti.user.UserImpl;

/**
 * Extracts the principal from the OAuth authentication
 * 
 * @author Ivan Prieto Barreiro
 */
@Component
public class OauthPrincipalExtractor implements PrincipalExtractor {

  @Override
  public Object extractPrincipal(Map<String, Object> map) {
    UserImpl user = new UserImpl();
    user.setEmployeeId((Integer) map.get("cern_person_id"));
    user.setUsername((String) map.get("cern_upn"));
    user.setFirstName((String) map.get("given_name"));
    user.setLastName((String) map.get("family_name"));
    user.setEmail((String) map.get("email"));
    return user;
  }
}
