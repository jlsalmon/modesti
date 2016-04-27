package cern.modesti.user;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface UserService {

  User findOneByUsername(String username);

  List<User> findByUsernameStartsWith(String query);

  List<User> findByNameAndGroup(String query, List<String> groups);

  User getCurrentUser();
}
