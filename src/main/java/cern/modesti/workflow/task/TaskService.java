package cern.modesti.workflow.task;

import cern.modesti.request.RequestRepository;
import cern.modesti.user.User;
import cern.modesti.user.UserService;
import cern.modesti.workflow.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
  private UserService userService;

  @Autowired
  private AuthService authService;

  @Autowired
  private org.activiti.engine.TaskService taskService;

  /**
   * @param requestId
   *
   * @return
   */
  public List<TaskInfo> getTasks(String requestId) {
    return taskService.createTaskQuery().processInstanceBusinessKey(requestId).list().stream().map(task ->
        new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(),
            authService.getCandidateGroups(task))).collect(Collectors.toList());
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

    return new TaskInfo(task.getName(), task.getDescription(), task.getOwner(), task.getAssignee(), task.getDelegationState(),
        authService.getCandidateGroups(task));
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
   *
   * @param requestId
   * @param taskName
   * @param username
   * @return
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
        authService.getCandidateGroups(task));
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
}
