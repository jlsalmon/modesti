package cern.modesti.workflow.signal;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import static java.lang.String.format;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * REST controller for retrieving and triggering signals into workflow process
 * instances.
 *
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/api/requests/{id}/signals")
public class SignalController {

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private TaskService taskService;

  @Autowired
  private RequestRepository requestRepository;

  @RequestMapping(method = GET)
  public ResponseEntity<Resources<Resource<SignalInfo>>> getSignals(@PathVariable("id") String id) {
    Request request = getRequest(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    List<SignalInfo> signals = new ArrayList<>();
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).active().singleResult();

    if (task == null) {
      return new ResponseEntity<>(new Resources<>(Collections.emptyList()), HttpStatus.OK);
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

  // TODO: add signal trigger endpoint

  private Request getRequest(String id) {
    return requestRepository.findOneByRequestId(id);
  }
}
