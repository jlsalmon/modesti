package cern.modesti.user;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * User interface for LDAP
 * 
 * @author Ivan Prieto Barreiro
 */
public interface LdapUser extends User, UserDetails {

}
