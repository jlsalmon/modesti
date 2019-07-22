package cern.modesti.workflow.task;

import static java.lang.String.format;

import java.util.Collections;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import cern.modesti.security.UserService;
import cern.modesti.user.User;
import cern.modesti.workflow.notification.CoreNotifications;
import cern.modesti.workflow.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;

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
public class UserTaskAssignmentHandler extends UserTaskParseHandler implements TaskListener {
  private static final long serialVersionUID = 2958253516432482700L;

  @Autowired
  private RequestService requestService;

  @Autowired
  private UserService userService;
  
  @Autowired
  private NotificationService notificationService;
  
  @Autowired
  org.activiti.engine.TaskService  taskService;
  
  @Override
  public void notify(DelegateTask task) {
    log.debug(format("handling assignment of task %s", task.getName()));

    // Save the new assignee to the request object
    Request request = requestService.findOneByRequestId((String) task.getVariable("requestId"));
    User currentUser = userService.getCurrentUser();

    if (task.getEventName().equals(TaskListener.EVENTNAME_ASSIGNMENT)) {
      User assignee = userService.findOneByUsername(task.getAssignee());
      String username = assignee == null ? null : assignee.getUsername();
      request.setAssignee(username);
      
      // Send a notification to the assignee if someone else assigned the request to him
      if (assignee != null && currentUser != null && !assignee.getEmployeeId().equals(currentUser.getEmployeeId())) {
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
    if (currentUser == null) {
      Authentication authentication = new UsernamePasswordAuthenticationToken("modesti", null,
          Collections.singletonList(new SimpleGrantedAuthority("modesti-administrators")));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    requestService.save(request);
  }

  @Override
  protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
    super.executeParse(bpmnParse, userTask);
    
    addListener(userTask, TaskListener.EVENTNAME_ASSIGNMENT);
    addListener(userTask, TaskListener.EVENTNAME_DELETE);
  }
  
  private void addListener(UserTask userTask, String event) {
    ActivitiListener listener = new ActivitiListener();
    listener.setEvent(event);
    listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_INSTANCE);
    listener.setInstance(this);
    userTask.getTaskListeners().add(listener);
  }
}
