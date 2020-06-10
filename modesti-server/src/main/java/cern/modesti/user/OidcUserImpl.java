package cern.modesti.user;

import java.util.Map;
import java.util.Set;

import org.springframework.hateoas.core.Relation;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Custom implementation of OidcUser for Oauth2
 * 
 * @author Ivan Prieto Barreiro
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Relation(value = "user", collectionRelation = "users")
public class OidcUserImpl extends AUser implements MyOidcUser {
  
  private Map<String, Object> claims;
  private OidcUserInfo userInfo;
  private OidcIdToken idToken;
  private Map<String, Object> attributes;
  
  public OidcUserImpl(OidcUser user, OidcIdToken idToken, Set<GrantedAuthority> authorities) {
    // cern_person_id does not exist for service accounts...
    super(user.getAttributes().get("cern_person_id") == null ? null :
          ((Long) user.getAttributes().get("cern_person_id")).intValue(),
        user.getName(), user.getGivenName(), user.getFamilyName(), user.getEmail(), authorities);
    this.claims = user.getClaims();
    this.userInfo = user.getUserInfo();
    this.idToken = idToken;
    this.attributes = user.getAttributes();
  }

  @Override
  public String getName() {
    return this.username;
  }
}
