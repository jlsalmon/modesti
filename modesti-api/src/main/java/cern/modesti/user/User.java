package cern.modesti.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * This class represents a single user entity retrieved from the authentication
 * context.
 *
 * @author Justin Lewis Salmon
 */
public interface User {

  Integer getEmployeeId();

  String getUsername();

  String getFirstName();

  String getLastName();
  
  String getMail();

  boolean isAdmin();
  
  Collection<? extends GrantedAuthority> getAuthorities();
}
