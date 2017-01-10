package cern.modesti.security.mock;

import cern.modesti.security.UserService;

/**
 * @author Justin Lewis Salmon
 */
public interface MockUserService extends UserService {

  void login(String username);
}
