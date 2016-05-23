package cern.modesti.workflow;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.request.Request;
import cern.modesti.security.UserService;
import cern.modesti.user.Role;
import cern.modesti.user.User;
import cern.modesti.workflow.task.TaskInfo;
import cern.modesti.workflow.task.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Service class for handling workflow authorisations.
 *
 * @author Justin Lewis Salmon
 */
@Service("authService")
@Slf4j
public class AuthService {

  @Autowired
  private TaskService taskService;

  @Autowired
  private UserService userService;

  /**
   * Check if the user is the creator of the given request.
   *
   * @param request the request object
   * @param user    the user obejct
   * @return true if the user is the creator of the request, false otherwise
   */
  public boolean isCreator(Request request, User user) {
    boolean creator = request.getCreator().equals(user.getUsername());
    log.debug(format("user %s is creator of request %s: %b", user.getUsername(), request.getRequestId(), creator));
    return creator;
  }

  /**
   * Check if a user is authorised to create {@link Request} instances for a
   * particular plugin.
   *
   * @param plugin the plugin to authorise
   * @param request the request to be created
   * @param user   the user to authorise
   * @return true if the user is authorised to create a request, false otherwise
   */
  public boolean isAuthorised(RequestProvider plugin, Request request, User user) {
    String authorisationGroup = plugin.getMetadata().getAuthorisationGroup(request);

    return user.isAdmin() || user.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals(authorisationGroup));
  }

  /**
   * Check if a user is authorised to act upon the currently active task of
   * the workflow process instance associated with the given request.
   *
   * @param request the request object
   * @param user    the user to authorise
   * @return true if the user is authorised, false otherwise
   */
  public boolean isAuthorised(Request request, User user) {
    TaskInfo currentTask = taskService.getActiveTask(request.getRequestId());
    return isAuthorised(currentTask, user);
  }

  /**
   * Check if a user is authorised to act upon the given task.
   *
   * @param task the task object
   * @param user the user to authorise
   * @return true if the user is authorised, false otherwise
   */
  public boolean isAuthorised(TaskInfo task, User user) {
    if (task == null) {
      return true;
    }

    if (user.isAdmin()) {
      log.info(format("authorising admin user %s", user.getUsername()));
      return true;
    }

    if (task.getCandidateGroups().isEmpty()) {
      log.debug(format("task %s specifies no candidate groups, hence user %s is authorised", task, user));
      return true;
    }

    // Find all users that are members of the candidate groups, and stop once we find one
    // that the given user is a member of
    List<User> users = userService.findByNameAndGroup(user.getUsername(), new ArrayList<>(task.getCandidateGroups()));

    for (User candidateUser : users) {
      if (candidateUser.getUsername().equals(user.getUsername())) {
        log.debug(format("user %s authorised for task %s", user.getUsername(), task));
        return true;
      }
    }

    return false;
  }
}
