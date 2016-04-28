package cern.modesti.workflow.task;

import cern.modesti.user.User;
import cern.modesti.security.UserService;
import cern.modesti.workflow.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Service class for retrieving/claiming/completing workflow tasks.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class TaskService {

  @Autowired
  private UserService userService;

  @Autowired
  private AuthService authService;

  @Autowired
  private org.activiti.engine.TaskService taskService;

  /**
   * Retrieve all tasks (active and suspended) from the workflow process
   * instance associated with a particular
   * {@link cern.modesti.request.Request} instance.
   *
   * @param requestId the id of the request
   * @return a list of tasks in the workflow process instance associated with
   * the request
   */
  public List<TaskInfo> getTasks(String requestId) {
    return taskService.createTaskQuery().processInstanceBusinessKey(requestId).list().stream().map(task ->
        new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(),
            getCandidateGroups(task))).collect(Collectors.toList());
  }

  /**
   * Retrieve a single task from the workflow process instance associated with
   * a particular {@link cern.modesti.request.Request} instance.
   *
   * @param requestId the id of the request
   * @param taskName  the name of the task
   * @return the task, or null if no task was found with the given name
   */
  public TaskInfo getTask(String requestId, String taskName) {
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).taskName(taskName).singleResult();

    if (task == null) {
      return null;
    }

    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Retrieve the currently active task from the workflow process instance
   * associated with a perticular {@link cern.modesti.request.Request}.
   *
   * @param requestId the id of the request
   * @return the currently active task, or null if no active task was found
   */
  public TaskInfo getActiveTask(String requestId) {
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).active().singleResult();

    if (task == null) {
      return null;
    }

    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Execute an action on a task inside a workflow process instance associated
   * with a {@link cern.modesti.request.Request}.
   *
   * @param requestId the id of the request
   * @param taskName  the name of the task to act upon
   * @param action    the action to execute
   * @param user      the user who is performing the action
   * @return the updated task (in case of a task assignment) or null (in case
   * of a task completion)
   */
  public TaskInfo execute(String requestId, String taskName, TaskAction action, User user) {
    TaskInfo currentTask = getTask(requestId, taskName);

    // Authorise the user to act upon this task
    if (!authService.isAuthorised(currentTask, user)) {
      throw new NotAuthorisedException(format("User %s is not authorised to perform action '%s' on task '%s' for request %s", user, action, taskName,
          requestId));
    }

    switch (action.getAction()) {
      case ASSIGN:
        return assignTask(requestId, taskName, action.getAssignee());
      case COMPLETE:
        completeTask(requestId, taskName);
        return null;
      default:
        throw new UnsupportedOperationException(format("'%s' is not a valid action", action.getAction()));
    }
  }

  /**
   * Assign a task inside a workflow process instance associated to a request
   * to the specified user.
   *
   * @param requestId the id of the request
   * @param taskName  the name of the task to assign
   * @param username  the username to assign the task to
   * @return the updated task info
   */
  private TaskInfo assignTask(String requestId, String taskName, String username) {
    Task task = getTaskForRequest(requestId, taskName);

    User user = userService.findOneByUsername(username);
    if (user == null) {
      throw new IllegalArgumentException("No user with username " + username + " was found");
    }

    task.setAssignee(username);
    // This will trigger an "assignment" event on the task, and also cause the assignee to be saved to the request object.
    taskService.setAssignee(task.getId(), username);

    task = getTaskForRequest(requestId, taskName);
    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(),
        getCandidateGroups(task));
  }

  /**
   * Complete a task inside a workflow process instance associated to a
   * request, i.e. push the request to the next task in the workflow.
   *
   * @param requestId the id of the request
   * @param taskName  the name of the task to complete
   */
  private void completeTask(String requestId, String taskName) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.complete(task.getId());
    // return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(), getCandidateGroups(task));
  }

  /**
   * Retrieve the set of candidate groups for a task.
   *
   * @param task the task object
   * @return the set of candidate group names
   */
  private Set<String> getCandidateGroups(Task task) {
    return taskService.getIdentityLinksForTask(task.getId()).stream().filter(link -> link.getType().equals(IdentityLinkType.CANDIDATE)).map
        (IdentityLink::getGroupId).collect(Collectors.toSet());
  }

  /**
   * Retrieve an internal {@link Task} instance by name for a particular
   * request.
   *
   * @param requestId the id of the request
   * @param taskName  the name of the task
   * @return the {@link Task} instance
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
}
