package cern.modesti.request;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.runtime.task.TaskResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import cern.modesti.schema.SchemaController;

@Component
public class RequestLinks {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLinks.class);

  @Autowired
  TaskService taskService;

  /**
   *
   * @param request
   * @return
   */
  Link getSchemaLink(Request request) {
    if (request.getCategories() != null) {

      // Need to manually build a comma-separated list of categories
      StringBuilder categories = new StringBuilder();
      for (String category : request.getCategories()) {
        categories.append(category).append(",");
      }

      if (categories.length() > 0) {
        categories.deleteCharAt(categories.length() - 1);
      }

      return linkTo(methodOn(SchemaController.class).getSchema(request.getRequestId(), categories.toString())).withRel("schema");
    } else {
      LOG.warn("Request " + request.getRequestId() + " has no schema link!");
      return null;
    }
  }

  /**
   *
   * @param request
   * @return
   */
  List<Link> getTaskLinks(Request request) {
    List<Link> links = new ArrayList<>();
    List<Task> tasks = taskService.createTaskQuery().processVariableValueEquals("requestId", request.getRequestId()).orderByTaskCreateTime().desc().list();

    for (Task task : tasks) {
      // The creator must submit the request
      //taskService.claim(task.getId(), request.getCreator());
      links.add(linkTo(methodOn(TaskResource.class).getTask(task.getId(), null)).withRel("tasks"));
    }

    return links;
  }
}
