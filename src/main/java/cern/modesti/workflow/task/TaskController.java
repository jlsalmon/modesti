package cern.modesti.workflow.task;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import cern.modesti.workflow.history.HistoricEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
 * /request/123/signals/split         POST
 * /request/123/signals/modify        POST
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

    List<TaskInfo> tasks = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).orderByTaskCreateTime().desc().list().stream()
        .map(t -> new TaskInfo(t.getName(), t.getDescription())).collect(Collectors.toList());

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

    Resource<TaskInfo> resource = new Resource<>(new TaskInfo(task.getName(), task.getDescription()));
    resource.add(linkTo(methodOn(TaskController.class).getTask(id, resource.getContent().getName())).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  /**
   * @param id
   * @param name
   *
   * @return
   */
  @RequestMapping(value = "/{name}", method = POST)
  public ResponseEntity action(@PathVariable("id") String id, @PathVariable("name") String name, @NotNull @RequestBody TaskAction action) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (action.getAction().equals(TaskAction.Action.DELEGATE)) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    if (action.getAction().equals(TaskAction.Action.CLAIM)) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    // Must be "complete" action
    completeTask(id, name);
    return new ResponseEntity(HttpStatus.OK);

  }

  /**
   * @param requestId
   * @param taskName
   */
  private void completeTask(String requestId, String taskName) {
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).taskName(taskName).singleResult();

    if (task == null) {
      List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(requestId).orderByTaskCreateTime().desc().list();

      throw new InvalidOperationException(format("Task '%s' does not exist or is not valid for request %s at this stage in the workflow. Available tasks: " +
          "[%s]", taskName, requestId, tasks.stream().map(Task::getName).collect(Collectors.joining(", "))));
    }

    taskService.complete(task.getId());
  }

  /**
   * @param id
   *
   * @return
   */
  private Request getRequest(String id) {
    return requestRepository.findOneByRequestId(id);
  }
}
