package cern.modesti.workflow.task;

import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import cern.modesti.user.User;
import cern.modesti.workflow.notification.CoreNotifications;
import cern.modesti.workflow.notification.NotificationService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

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
  private RequestService requestService;

  @Autowired
  private UserService userService;
  
  @Autowired
  private NotificationService notificationService;

  @Override
  public void notify(DelegateTask task) {
    log.debug(format("handling assignment of task %s", task.getName()));

    // Save the new assignee to the request object
    Request request = requestService.findOneByRequestId((String) task.getVariable("requestId"));

    if (task.getEventName().equals(TaskListener.EVENTNAME_ASSIGNMENT)) {
      User assignee = userService.findOneByUsername(task.getAssignee());
      String username = assignee == null ? null : assignee.getUsername();
      request.setAssignee(username);
      if (assignee != null) {
        notificationService.sendNotification(CoreNotifications.requestAssignedToUser(request));
      }
    } else if (task.getEventName().equals(TaskListener.EVENTNAME_DELETE)) {
      request.setAssignee(null);
    }

    // It's possible that this listener will be invoked outside of the Spring
    // Security filter chain, in which case calling RequestService#save will
    // fail because the security context will be empty. To prevent this, we
    // manually set the authentication context with the details of the modesti
    // service account (which has admin privileges).
    if (userService.getCurrentUser() == null) {
      Authentication authentication = new UsernamePasswordAuthenticationToken("modesti", null,
          Collections.singletonList(new SimpleGrantedAuthority("modesti-administrators")));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    requestService.save(request);
  }

  @Override
  protected Class< ? extends BaseElement> getHandledType() {
    return UserTask.class;
  }

  @Override
  protected void executeParse(BpmnParse bpmnParse, UserTask element) {
    TaskDefinition taskDefinition = (TaskDefinition) bpmnParse.getCurrentActivity().getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, this);
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_DELETE, this);
  }
}
