package cern.modesti.workflow.task;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.user.User;
import cern.modesti.security.UserService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.task.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

/**
 * Custom {@link TaskListener} registered globally via custom
 * {@link org.activiti.engine.parse.BpmnParseHandler} that listens for all
 * {@literal assignment} events on user tasks and synchronises the new assignee
 * to the corresponding {@link Request} object.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class UserTaskAssignmentHandler extends AbstractBpmnParseHandler<UserTask> implements TaskListener {

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private UserService userService;

  @Override
  public void notify(DelegateTask task) {
    log.debug(format("handling assignment of task %s", task.getName()));

    // Save the new assignee to the request object
    Request request = requestRepository.findOneByRequestId((String) task.getVariable("requestId"));
    User assignee = userService.findOneByUsername(task.getAssignee());
    request.setAssignee(assignee.getUsername());
    requestRepository.save(request);
  }

  @Override
  protected Class< ? extends BaseElement> getHandledType() {
    return UserTask.class;
  }

  @Override
  protected void executeParse(BpmnParse bpmnParse, UserTask element) {
    TaskDefinition taskDefinition = (TaskDefinition) bpmnParse.getCurrentActivity().getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, this);
  }
}
