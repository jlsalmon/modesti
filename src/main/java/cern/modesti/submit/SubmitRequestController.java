/**
 *
 */
package cern.modesti.submit;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 *
 */
@Controller
public class SubmitRequestController {

  @Autowired
  SubmitRequestService service;

  @RequestMapping(value = "/requests/{id}/submit", method = POST)
  public ResponseEntity submitRequest(@PathVariable("id") String id) {
    service.submitRequest(id);
    return new ResponseEntity(HttpStatus.OK);
  }

  @RequestMapping(value="/users/{id}/tasks", method= RequestMethod.GET)
  public HttpEntity<Resources<TaskRepresentation>> getTasks(@PathVariable("id") String id) {
    List<Task> tasks = service.getTasks();

    List<TaskRepresentation> dtos = new ArrayList<>();
    for (Task task : tasks) {
      dtos.add(new TaskRepresentation(task.getId(), task.getName()));
    }

    Resources<TaskRepresentation> resources = new Resources<>(dtos);
    return new ResponseEntity<>(resources, HttpStatus.OK);
  }



  /**
   *
   */
  static class TaskRepresentation {

    private String id;
    private String name;

    public TaskRepresentation(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public String getId() {
      return id;
    }
    public void setId(String id) {
      this.id = id;
    }
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
  }
}
