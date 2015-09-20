package cern.modesti.security;

import cern.modesti.request.Request;
import cern.modesti.user.User;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service("authService")
@Slf4j
public class AuthService {

  @Autowired
  private TaskService taskService;

  /**
   *
   * @param request
   * @param user
   * @return
   */
  public boolean isCreator(Request request, User user) {
    boolean creator = request.getCreator().getUsername().equals(user.getUsername());
    log.debug(format("user %s is creator of request %s: %b", user.getUsername(), request.getRequestId(), creator));
    return creator;
  }

  /**
   *
   * @param request
   * @param user
   * @return
   */
  public boolean isAssigned(Request request, User user) {
    boolean assigned = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).taskAssignee(user.getUsername()).list().size() > 0;
    log.debug(format("user %s is assigned to at least one task on request %s: %b", user.getUsername(), request.getRequestId(), assigned));
    return assigned;
  }
}
