package cern.modesti.user;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * This class represents a single user entity retrieved from the authentication
 * context.
 *
 * @author Justin Lewis Salmon
 */
public interface User extends UserDetails {

  Integer getEmployeeId();

  String getUsername();

  String getFirstName();

  String getLastName();

  String getEmail();

  boolean isAdmin();
}
