package cern.modesti.workflow.task;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.user.User;
import cern.modesti.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class TaskService {

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private org.activiti.engine.TaskService taskService;

  /**
   * @param requestId
   *
   * @return
   */
  public List<TaskInfo> getTasks(String requestId) {
    return taskService.createTaskQuery().processInstanceBusinessKey(requestId).list().stream().map(task -> new TaskInfo(task.getName(), task.getDescription()
        , task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task))).collect(Collectors.toList());
  }

  /**
   * @param requestId
   * @param taskName
   *
   * @return
   */
  public TaskInfo getTask(String requestId, String taskName) {
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).taskName(taskName).singleResult();

    if (task == null) {
      return null;
    }

    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * @param requestId
   * @param taskName
   * @param action
   * @param user
   *
   * @return
   */
  public TaskInfo execute(String requestId, String taskName, TaskAction action, User user) {
    Task currentTask = getTaskForRequest(requestId, taskName);

    // Authorise the user to act upon this task
    if (!isUserAuthorisedFor(currentTask, user)) {
      throw new NotAuthorisedException(format("User %s is not authorised to perform action '%s' on task '%s' for request %s", user, action, taskName,
          requestId));
    }

    switch (action.getAction()) {
      case ASSIGN:
        return assignTask(requestId, taskName, action.getAssignee());
      case CLAIM:
        return claimTask(requestId, taskName, action.getAssignee());
      case COMPLETE:
        completeTask(requestId, taskName);
        return null;
      case DELEGATE:
        return delegateTask(requestId, taskName, action.getAssignee());
      case RESOLVE:
        return resolveTask(requestId, taskName);
      case UNCLAIM:
        return unclaimTask(requestId, taskName);
      default:
        throw new UnsupportedOperationException(format("'%s' is not a valid action", action.getAction()));
    }
  }

  /**
   *
   * @param requestId
   * @param taskName
   * @param username
   * @return
   */
  private TaskInfo assignTask(String requestId, String taskName, String username) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.setAssignee(task.getId(), username);

    // Set the assignee on the request object
    User user = userRepository.findOneByUsername(username);
    if (user == null) {
      throw new IllegalArgumentException("No user with username " + username + " was found");
    }

    Request request = requestRepository.findOneByRequestId(requestId);
    request.setAssignee(user);
    requestRepository.save(request);

    task = getTaskForRequest(requestId, taskName);
    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Claim a task, i.e. set the given user as the owner and assignee of the task.
   *
   * @param requestId
   * @param taskName
   * @param username
   */
  private TaskInfo claimTask(String requestId, String taskName, String username) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.claim(task.getId(), username);
    taskService.setOwner(task.getId(), username);
    task = getTaskForRequest(requestId, taskName);
    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Complete a task, i.e. push the request to the next task in the workflow. Tasks can only be completed by the task owner, not by delegated users.
   *
   * @param requestId
   * @param taskName
   */
  private void completeTask(String requestId, String taskName) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.complete(task.getId());
    // return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Delegate a task, i.e. transfer a task to another user for them to process. Once the other user has finished, they will then "resolve" the task.
   *
   * @param requestId
   * @param taskName
   * @param assignee
   */
  private TaskInfo delegateTask(String requestId, String taskName, String assignee) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.delegateTask(task.getId(), assignee);
    task = getTaskForRequest(requestId, taskName);
    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Resolve a task, i.e. signal that the work has been done on a task and that it should be transferred back to the task owner for completion.
   *
   * @param requestId
   * @param taskName
   */
  private TaskInfo resolveTask(String requestId, String taskName) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.resolveTask(task.getId());
    task = getTaskForRequest(requestId, taskName);
    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Unclaim a task, i.e. put it back into the pool of available tasks.
   *
   * @param requestId
   * @param taskName
   *
   * @return
   */
  private TaskInfo unclaimTask(String requestId, String taskName) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.unclaim(task.getId());
    task = getTaskForRequest(requestId, taskName);
    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * @param requestId
   *
   * @return
   */
  private Request getRequest(String requestId) {
    return requestRepository.findOneByRequestId(requestId);
  }

  /**
   * @param requestId
   * @param taskName
   *
   * @return
   */
  private Task getTaskForRequest(String requestId, String taskName) {
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).taskName(taskName).active().singleResult();

    if (task == null) {
      List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(requestId).orderByTaskCreateTime().desc().list();

      throw new IllegalArgumentException(format("Task '%s' does not exist or is not valid for request %s at this stage in the workflow. Available tasks: " +
          "[%s]", taskName, requestId, tasks.stream().map(Task::getName).collect(Collectors.joining(", "))));
    }

    return task;
  }

  /**
   * @param task
   * @param user
   *
   * @return
   */
  private boolean isUserAuthorisedFor(Task task, User user) {
    Set<String> roles = getRoles(user);
    Set<String> candidateGroups = getCandidateGroups(task);

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
   *
   * @return
   */
  private Set<String> getCandidateGroups(Task task) {
    return taskService.getIdentityLinksForTask(task.getId()).stream().filter(link -> link.getType().equals(IdentityLinkType.CANDIDATE)).map
        (IdentityLink::getGroupId).collect(Collectors.toSet());
  }

  /**
   * @param user
   *
   * @return
   */
  private Set<String> getRoles(User user) {
    return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
  }
}
