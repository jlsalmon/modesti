package cern.modesti.security.oauth;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.CollectionUtils;

import cern.modesti.user.OidcUserImpl;
import lombok.RequiredArgsConstructor;

/**
 * Class used to map OIDC authorities
 * 
 * @author Ivan Prieto Barreiro
 */
@RequiredArgsConstructor
public class KeycloakOauth2UserService extends OidcUserService {

  private final OAuth2Error invalidRequest = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
  private final JwtDecoder jwtDecoder;

  /**
   * Augments {@link OidcUserService#loadUser(OidcUserRequest)} to add authorities
   * provided by Keycloak.
   * 
   * Needed because {@link OidcUserService#loadUser(OidcUserRequest)} (currently)
   * does not provide a hook for adding custom authorities from a
   * {@link OidcUserRequest}.
   */
  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) {
    OidcUser user = super.loadUser(userRequest);

    Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    authorities.addAll(extractKeycloakAuthorities(userRequest));

    return new OidcUserImpl(user, userRequest.getIdToken(), authorities);
  }

  /**
   * Extracts {@link GrantedAuthority GrantedAuthorities} from the AccessToken in
   * the {@link OidcUserRequest}.
   * 
   * @param userRequest
   * @return
   */
  private Collection<? extends GrantedAuthority> extractKeycloakAuthorities(OidcUserRequest userRequest) {
    Jwt token = parseJwt(userRequest.getAccessToken().getTokenValue());

    @SuppressWarnings("unchecked")
    Map<String, Object> resourceMap = (Map<String, Object>) token.getClaims().get("resource_access");
    String clientId = userRequest.getClientRegistration().getClientId();

    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> clientResource = (Map<String, Map<String, Object>>) resourceMap.get(clientId);
    if (CollectionUtils.isEmpty(clientResource)) {
      return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    List<String> clientRoles = (List<String>) clientResource.get("roles");
    if (CollectionUtils.isEmpty(clientRoles)) {
      return Collections.emptyList();
    }

    Collection<? extends GrantedAuthority> authorities = AuthorityUtils
                    .createAuthorityList(clientRoles.toArray(new String[0]));

    return authorities;
  }

  private Jwt parseJwt(String accessTokenValue) {
    try {
      // Token is already verified by spring security infrastructure
      return jwtDecoder.decode(accessTokenValue);
    } catch (JwtException e) {
      throw new OAuth2AuthenticationException(invalidRequest, e);
    }
  }
}