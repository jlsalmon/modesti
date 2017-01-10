package cern.modesti.security;

import cern.modesti.user.User;

import java.util.List;

/**
 * Service class for searching for {@link User}s in the authentication context.
 *
 * @author Justin Lewis Salmon
 */
public interface UserService {

  /**
   * Find a single {@link User} by exact username.
   *
   * @param username the username to search for
   * @return the {@link User} having the given username, or null if none was found
   */
  User findOneByUsername(String username);

  /**
   * Find a list of {@link User}s by username prefix.
   *
   * @param query the username prefix to search for
   * @return a list of found {@link User}s, or an empty list if none were found
   */
  List<User> findByUsernameStartsWith(String query);

  /**
   * Find a list of {@link User}s by username, first name or last name prefix
   * and group memberships.
   *
   * @param query  the username/first name/last name prefix query
   * @param groups a list of groups to which a user must belong to be included
   *               in the search result
   * @return a list of found {@link User}s, or an empty list if none were found
   */
  List<User> findByNameAndGroup(String query, List<String> groups);

  /**
   * @return the current logged in user.
   */
  User getCurrentUser();
}
