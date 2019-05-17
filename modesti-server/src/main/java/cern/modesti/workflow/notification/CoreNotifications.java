package cern.modesti.workflow.notification;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.modesti.request.Request;
import cern.modesti.security.UserService;
import cern.modesti.user.User;

import static java.lang.String.format;

/**
 * Core notifications component, creates notifications to be sent by the server
 *  
 * @author Ivan Prieto Barreiro
 */
@Component
public class CoreNotifications {

  private static CoreNotifications self;
  
  @Autowired
  private UserService userService;
  
  /**
   * Registers the class singleton instance
   */
  @PostConstruct
  public void registerInstance() {
    setInstance(this);
  }
  
  private static void setInstance(CoreNotifications coreNotifications) {
    CoreNotifications.self = coreNotifications;
  }
  
  /**
   * Notification created when a request has been assigned to a user
   * @param request The original request
   * @return Notification to be sent to the assignee
   */
  public static Notification requestAssignedToUser(Request request) {
    if (request.getAssignee() == null) {
      return null;
    }
    
    User assignee = self.userService.findOneByUsername(request.getAssignee());
    User creator = self.userService.findOneByUsername(request.getCreator());
    
    return Notification.builder()
        .request(request)
        .subject(format("A MODESTI request has been assigned to you (#%s)", request.getRequestId()))
        .template("request-assigned-to-user")
        .templateParameter("creator", creator)
        .recipient(assignee.getMail())
        .build();
  }
}
