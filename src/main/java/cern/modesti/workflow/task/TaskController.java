package cern.modesti.workflow.task;

import cern.modesti.security.ldap.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

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
 * /requests                        GET, POST, PUT, DELETE    {request}
 * /requests/upload                 POST                      {.xls|.xlsx}
 * /requests/123/tasks/validate     POST                      {action: 'complete|delegate'}
 * /requests/123/tasks/submit       POST                      {action: 'complete|delegate'}
 * /requests/123/tasks/approve      POST                      {action: 'claim|complete|delegate'}
 * /requests/123/tasks/address      POST                      {action: 'claim|complete|delegate'}
 * /requests/123/tasks/cable        POST                      {action: 'claim|complete|delegate'}
 * /requests/123/tasks/configure    POST                      {action: 'complete|delegate'}
 * /requests/123/tasks/test         POST                      {action: 'claim|complete|delegate'}
 * /requests/123/signals/split      POST
 * /requests/123/signals/modify     POST
 * /requests/123/history            GET
 * /requests/123/progress           GET
 *
 * /metrics                         GET
 *
 * /schemas                         GET, POST, PUT, DELETE    {schema}
 * /schemas/TIM                     GET                       {schema}
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

  /**
   * @param id
   *
   * @return
   */
  @RequestMapping(method = GET)
  public ResponseEntity<Resources<Resource<TaskInfo>>> getTasks(@PathVariable("id") String id) {
    List<TaskInfo> tasks = taskService.getTasks(id);

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
    TaskInfo task = taskService.getTask(id, name);
    if (task == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Resource<TaskInfo> resource = new Resource<>(task);
    resource.add(linkTo(methodOn(TaskController.class).getTask(id, resource.getContent().getName())).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  /**
   * @param id
   * @param taskName
   *
   * @return
   */
  @RequestMapping(value = "/{name}", method = POST)
  public HttpEntity<Resource<TaskInfo>> action(@PathVariable("id") String id, @PathVariable("name") String taskName, @RequestBody TaskAction action,
                                               Principal principal) {
    User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    TaskInfo task = taskService.execute(id, taskName, action, user);

    if (task != null) {
      Resource<TaskInfo> resource = new Resource<>(task);
      resource.add(linkTo(methodOn(TaskController.class).getTask(id, resource.getContent().getName())).withSelfRel());
      return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
