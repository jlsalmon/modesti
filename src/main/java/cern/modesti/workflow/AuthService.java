package cern.modesti.workflow;

import cern.modesti.request.Request;
import cern.modesti.user.User;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

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
  private org.activiti.engine.TaskService taskService;

  /**
   * @param request
   * @param user
   * @return
   */
  public boolean isCreator(Request request, User user) {
    boolean creator = request.getCreator().equals(user.getUsername());
    log.debug(format("user %s is creator of request %s: %b", user.getUsername(), request.getRequestId(), creator));
    return creator;
  }

  /**
   * @param request
   * @param user
   * @return
   */
  public boolean isAuthorised(Request request, User user) {
    Task currentTask = getTaskForRequest(request.getRequestId());
    return isAuthorised(currentTask, user);
  }

  /**
   * @param task
   * @param user
   * @return
   */
  public boolean isAuthorised(Task task, User user) {
    if (task == null) {
      return true;
    }

    Set<String> roles = getRoles(user);
    Set<String> candidateGroups = getCandidateGroups(task);

    if (candidateGroups.isEmpty()) {
      log.debug(format("task %s specifies no candidate groups, hence user %s is authorised", task, user));
      return true;
    }

    for (String candidateGroup : candidateGroups) {
      if (roles.contains(candidateGroup)) {
        log.debug(format("user %s authorised for task %s", user, task));
        return true;
      }
    }

    return false;
  }

  /**
   * @param task
   * @return
   */
  public Set<String> getCandidateGroups(Task task) {
    return taskService.getIdentityLinksForTask(task.getId()).stream().filter(link -> link.getType().equals(IdentityLinkType.CANDIDATE)).map
        (IdentityLink::getGroupId).collect(Collectors.toSet());
  }

  /**
   * @param user
   * @return
   */
  private Set<String> getRoles(User user) {
    return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
  }

  /**
   * @param requestId
   * @return
   */
  private Task getTaskForRequest(String requestId) {
    return taskService.createTaskQuery().processInstanceBusinessKey(requestId).active().singleResult();
  }
}
