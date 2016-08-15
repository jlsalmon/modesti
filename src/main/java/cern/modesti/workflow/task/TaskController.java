package cern.modesti.workflow.task;

import cern.modesti.user.User;
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
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
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
 * REST controller for retrieving and executing actions on workflow tasks for
 * specific requests.
 *
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/api/requests/{id}/tasks")
@PreAuthorize("permitAll")
@Slf4j
public class TaskController {

  @Autowired
  private TaskService taskService;

  @RequestMapping(method = GET, produces = "application/hal+json")
  public ResponseEntity<Resources<Resource<TaskInfo>>> getTasks(@PathVariable("id") String id) {
    List<TaskInfo> tasks = taskService.getTasks(id);

    Resources<Resource<TaskInfo>> resources = Resources.wrap(tasks);
    for (Resource<TaskInfo> resource : resources) {
      resource.add(linkTo(methodOn(TaskController.class).getTask(id, resource.getContent().getName())).withSelfRel());
    }

    return new ResponseEntity<>(resources, HttpStatus.OK);
  }

  @RequestMapping(value = "/{name}", method = GET, produces = "application/hal+json")
  public ResponseEntity<Resource<TaskInfo>> getTask(@PathVariable("id") String id, @PathVariable("name") String name) {
    TaskInfo task = taskService.getTask(id, name);
    if (task == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Resource<TaskInfo> resource = new Resource<>(task);
    resource.add(linkTo(methodOn(TaskController.class).getTask(id, resource.getContent().getName())).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  @RequestMapping(value = "/{name}", method = POST, produces = "application/hal+json")
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
