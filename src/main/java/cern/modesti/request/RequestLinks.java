package cern.modesti.request;

import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaController;
import cern.modesti.workflow.signal.SignalController;
import cern.modesti.workflow.task.TaskController;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class RequestLinks {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLinks.class);

  @Autowired
  TaskService taskService;

  @Autowired
  RuntimeService runtimeService;

  @Autowired
  EntityLinks entityLinks;

  /**
   *
   * @param request
   * @return
   */
  Link getSchemaLink(Request request) {
    return linkTo(methodOn(SchemaController.class).getSchema(request.getRequestId())).withRel("schema");
  }

  /**
   *
   * @param request
   * @return
   */
  List<Link> getTaskLinks(Request request) {
    List<Link> links = new ArrayList<>();
    List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).orderByTaskCreateTime().desc().list();

    for (Task task : tasks) {
      links.add(linkTo(TaskController.class, request.getRequestId()).slash(task.getName()).withRel("tasks"));
    }

    return links;
  }

  /**
   *
   * @param request
   * @return
   */
  List<Link> getSignalLinks(Request request) {
    List<Link> links = new ArrayList<>();
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).active().singleResult();

    // Query the signals that are subscribed to by the current process instance.
    // TODO this is a non-public API, is there a supported way of doing this?
    CommandExecutor executor = ((ProcessEngineConfigurationImpl) ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration()).getCommandExecutor();
    EventSubscriptionQueryImpl query = new EventSubscriptionQueryImpl(executor);
    List<EventSubscriptionEntity> signals = query.processInstanceId(task.getProcessInstanceId()).list();

    for (EventSubscriptionEntity signal : signals) {
      links.add(linkTo(SignalController.class, request.getRequestId()).slash(signal.getEventName()).withRel("signals"));
    }

    return links;
  }
}
