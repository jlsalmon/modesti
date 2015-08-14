package cern.modesti.workflow.task;

import static java.lang.String.format;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import cern.modesti.workflow.task.TaskAction.Action;
import lombok.extern.slf4j.Slf4j;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import cern.modesti.security.ldap.User;

/**
 * TODO
 *
 * REST endpoints:
 *
 * URL                              Allowed methods           Request body
 * -----------------------------------------------------------------------------------
 * /metrics                         GET
 * /requests                        GET, POST, PUT, DELETE    {request}
 * /requests/upload                 POST                      {.xls|.xlsx}
 *
 * /request/123/tasks/validate      POST                      {action: 'complete|delegate'}
 * /request/123/tasks/submit        POST                      {action: 'complete|delegate'}
 * /request/123/tasks/approve       POST                      {action: 'claim|complete|delegate'}
 * /request/123/tasks/address       POST                      {action: 'claim|complete|delegate'}
 * /request/123/tasks/cable         POST                      {action: 'claim|complete|delegate'}
 * /request/123/tasks/configure     POST                      {action: 'complete|delegate'}
 * /request/123/tasks/test          POST                      {action: 'claim|complete|delegate'}
 *
 * /request/123/signals/split       POST
 * /request/123/signals/modify      POST
 *
 *
 * /request/123/history             GET
 * /request/123/schema              GET
 * /request/123/progress            GET
 *
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/requests/{id}/tasks")
@PreAuthorize("permitAll")
@Slf4j
public class TaskController {

  @Autowired
  private TaskService taskService;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private RequestRepository requestRepository;

  /**
   * @param id
   *
   * @return
   */
  @RequestMapping(method = GET)
  public ResponseEntity<Resources<Resource<TaskInfo>>> getTasks(@PathVariable("id") String id) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    List<TaskInfo> tasks = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).list().stream().map(task -> new TaskInfo(task
        .getName(), task.getDescription(), task.getAssignee(), getCandidateGroups(task))).collect(Collectors.toList());

    Resources<Resource<TaskInfo>> resources = Resources.wrap(tasks);
    for (Resource<TaskInfo> resource : resources) {
      resource.add(linkTo(methodOn(TaskController.class).getTask(id, resource.getContent().getName())).withSelfRel());
    }

    return new ResponseEntity<>(resources, HttpStatus.OK);
  }

  /**
   * @param id
   * @param name
   *
   * @return
   */
  @RequestMapping(value = "/{name}", method = GET)
  public ResponseEntity<Resource<TaskInfo>> getTask(@PathVariable("id") String id, @PathVariable("name") String name) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Task task = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).taskName(name).singleResult();
    if (task == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Resource<TaskInfo> resource = new Resource<>(new TaskInfo(task.getName(), task.getDescription(), task.getAssignee(), getCandidateGroups(task)));
    resource.add(linkTo(methodOn(TaskController.class).getTask(id, resource.getContent().getName())).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  /**
   * @param requestId
   * @param taskName
   *
   * @return
   */
  @RequestMapping(value = "/{name}", method = POST)
  public HttpEntity<Resource<TaskInfo>> action(@PathVariable("id") String requestId, @PathVariable("name") String taskName, @RequestBody TaskAction action,
                                               Principal principal) {
    Request request = getRequest(requestId);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Task currentTask = getTaskForRequest(requestId, taskName);

    // Authorise the user to act upon this task
    if (!isUserAuthorisedFor(currentTask, principal)) {
      return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    if (action.getAction().equals(Action.CLAIM)) {
      TaskInfo task = claimTask(requestId, taskName, action.getAssignee());
      return new ResponseEntity<>(new Resource<>(task), HttpStatus.OK);
    } else if (action.getAction().equals(Action.COMPLETE)) {
      completeTask(requestId, taskName);
      return new ResponseEntity(HttpStatus.OK);
    } else if (action.getAction().equals(Action.DELEGATE)) {
      throw new UnsupportedOperationException("Not yet implemented");
    } else {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * @param requestId
   * @param taskName
   * @param assignee
   */
  private TaskInfo claimTask(String requestId, String taskName, String assignee) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.claim(task.getId(), assignee);
    task.setAssignee(assignee);
    return new TaskInfo(task.getName(), task.getDescription(), task.getAssignee(), getCandidateGroups(task));
  }

  /**
   * @param requestId
   * @param taskName
   */
  private void completeTask(String requestId, String taskName) {
    Task task = getTaskForRequest(requestId, taskName);
    taskService.complete(task.getId());
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
   * @param principal
   *
   * @return
   */
  private boolean isUserAuthorisedFor(Task task, Principal principal) {
    User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

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
    return taskService.getIdentityLinksForTask(task.getId()).stream().map(IdentityLink::getGroupId).collect(Collectors.toSet());
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
