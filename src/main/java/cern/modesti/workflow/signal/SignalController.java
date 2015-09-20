package cern.modesti.workflow.signal;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.user.User;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/requests/{id}/signals")
public class SignalController {

  @Autowired
  private RuntimeService runtimeService;

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
  public ResponseEntity<Resources<Resource<SignalInfo>>> getSignals(@PathVariable("id") String id) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    List<SignalInfo> signals = new ArrayList<>();
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).active().singleResult();

    if (task == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Query the signals that are subscribed to by the current process instance.
    // TODO this is a non-public API, is there a supported way of doing this?
    CommandExecutor executor = ((ProcessEngineConfigurationImpl) ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration()).getCommandExecutor();
    EventSubscriptionQueryImpl query = new EventSubscriptionQueryImpl(executor);

    for (EventSubscriptionEntity signal : query.processInstanceId(task.getProcessInstanceId()).list()) {
      signals.add(new SignalInfo(signal.getEventName()));
    }

    Resources<Resource<SignalInfo>> resources = Resources.wrap(signals);
    for (Resource<SignalInfo> resource : resources) {
      resource.add(linkTo(methodOn(SignalController.class).getSignal(id, resource.getContent().getName())).withSelfRel());
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
  public ResponseEntity<Resource<SignalInfo>> getSignal(@PathVariable("id") String id, @PathVariable("name") String name) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).list();

    for (Task task : tasks) {
      Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId())
          .signalEventSubscriptionName(name).singleResult();

      if (execution != null) {
        Resource<SignalInfo> resource = new Resource<>(new SignalInfo(name));
        resource.add(linkTo(methodOn(SignalController.class).getSignal(id, resource.getContent().getName())).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.OK);
      }
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * @param id
   *
   * @return
   */
  @RequestMapping(value = "/requestModified", method = POST)
  public ResponseEntity requestModified(@PathVariable("id") String id, Principal principal) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Task task = taskService.createTaskQuery().processInstanceBusinessKey(id).taskName("submit").singleResult();
    if (task == null) {
      throw new InvalidOperationException(format("Signal 'requestModified' is not valid for request %s at this stage in the workflow.", id));
    }

    // Send the signal to the workflow engine, which will sent the request back to the "edit" task
    runtimeService.signalEventReceived("requestModified", task.getExecutionId());

    // Claim the "edit" task as the user who just modified the request.
    task = taskService.createTaskQuery().processInstanceBusinessKey(id).taskName("edit").singleResult();
    User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    taskService.claim(task.getId(), user.getUsername());
    task.setAssignee(user.getUsername());

    return new ResponseEntity(HttpStatus.OK);
  }

  /**
   * @param id
   *
   * @return
   */
  @RequestMapping(value = "/splitRequest", method = POST)
  public ResponseEntity splitRequest(@PathVariable("id") String id, @RequestBody List<Long> pointIdsToSplit) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Task task = taskService.createTaskQuery().processInstanceBusinessKey(id).active().singleResult();
    if (task == null) {
      throw new InvalidOperationException(format("Signal 'splitRequest' is not valid for request %s at this stage in the workflow.", id));
    }

    Map<String, Object> variables = new HashMap<>();
    variables.put("points", pointIdsToSplit);

    runtimeService.signalEventReceived("splitRequest", task.getExecutionId(), variables);
    return new ResponseEntity(HttpStatus.OK);
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
