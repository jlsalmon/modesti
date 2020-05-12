package cern.modesti.user;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * User interface for Oauth2 OidcUser representation
 *  
 * @author Ivan Prieto Barreiro
 */
public interface MyOidcUser extends User, OidcUser {

}
