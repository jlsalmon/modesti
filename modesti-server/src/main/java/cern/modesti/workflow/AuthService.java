package cern.modesti.workflow;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.plugin.spi.AuthorizationProvider;
import cern.modesti.request.Request;
import cern.modesti.security.UserService;
import cern.modesti.user.User;
import cern.modesti.workflow.task.TaskInfo;
import cern.modesti.workflow.task.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Service class for handling workflow authorisations.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
@Service
public class AuthService {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private TaskService taskService;

  @Autowired
  private UserService userService;

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  /**
   * Check if the user is the creator of the given request.
   *
   * @param request the request object
   * @param user    the user obejct
   *
   * @return true if the user is the creator of the request, false otherwise
   */
  public boolean isCreator(Request request, User user) {
    boolean creator = request.getCreator().equals(user.getUsername());
    log.debug(format("user %s is creator of request %s: %b", user.getUsername(), request.getRequestId(), creator));
    return creator;
  }

  /**
   * Check if a user is authorised to create {@link Request} instances for a
   * particular plugin.
   *
   * @param plugin  the plugin to authorise
   * @param request the request to be created
   * @param user    the user to authorise
   *
   * @return true if the user is authorised to create a request, false otherwise
   */
  public boolean canCreate(RequestProvider plugin, Request request, User user) {
    String authorisationGroup = plugin.getMetadata().getAuthorisationGroup(request);
    return isAdministrator(user) || user.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals(authorisationGroup));
  }

  /**
   * Check if a user is authorized to act upon the currently active task of
   * the workflow process instance associated with the given request.
   *
   * @param request the request object
   * @param user    the user to authorize
   *
   * @return true if the user is authorized, false otherwise
   */
  public boolean canSave(Request request, User user) {
    TaskInfo currentTask = taskService.getActiveTask(request.getRequestId());
    return isAdministrator(user) || isCreator(request, user) || userAuthorisedForTask(currentTask, user);
  }

  /**
   * Check if a user is authorized to save the given request.
   *
   * @param request the request object
   * @param user    the user to authorize
   *
   * @return true if the user is authorized, false otherwise
   */
  public boolean canSave(Request request, String username) {
    User user;

    if ("principal".equals(username)) {
      user = userService.getCurrentUser();
    } else {
      user = userService.findOneByUsername(username);
    }

    return canSave(request, user);
  }

  /**
   * Check if a user is authorised to act upon the given task.
   *
   * @param task the task object
   * @param user the user to authorise
   *
   * @return true if the user is authorised, false otherwise
   */
  public boolean userAuthorisedForTask(TaskInfo task, User user) {
    if (task == null) {
      return true;
    }

    if (isAdministrator(user)) {
      log.info(format("authorising admin user %s", user.getUsername()));
      return true;
    }

    if (task.getCandidateGroups().isEmpty()) {
      log.debug(format("task %s specifies no candidate groups, hence user %s is authorised", task, user));
      return true;
    }

    // Find all users that are members of the candidate groups, and stop once we find one
    // that the given user is a member of
    List<User> users = userService.findByNameAndGroup(user.getUsername(), new ArrayList<>(task.getCandidateGroups()));

    for (User candidateUser : users) {
      if (candidateUser.getUsername().equals(user.getUsername())) {
        log.debug(format("user %s authorised for task %s", user.getUsername(), task));
        return true;
      }
    }

    return false;
  }

  /**
   * Default is only creator is allowed to delete. Administrators are always allowed to delete.
   * Plugins can implement the {@link AuthorizationProvider} to overwrite the {@link AuthorizationProvider#canDelete(Request)} behaviour.
   *
   * @param request the request object
   * @param user    the user to authorize
   *
   * @return true if the user is authorized, false otherwise
   */
  public boolean canDelete(Request request, User user) {
    RequestProvider plugin = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));
    String requestPluginId = plugin.getMetadata().getId();

    if (isAdministrator(user)) {
      return true;
    }

    String pluginAuthrorizationGroup = plugin.getMetadata().getAuthorisationGroup(request);
    if (hasRole(user, pluginAuthrorizationGroup)) {
      return true;
    }

    AuthorizationProvider authProvider = getPluginAuthorizationProvider(requestPluginId);
    if (authProvider != null) {
      return authProvider.canDelete(request);
    }

    return request.getCreator().equals(user.getUsername());
  }
  
  private boolean hasRole(User user, String role) {
    return user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(role));
  }

  private boolean isAdministrator(User user) {
    return user.getAuthorities().stream().anyMatch(role -> "modesti-administrators".equals(role.getAuthority()));
  }

  private AuthorizationProvider getPluginAuthorizationProvider(String requestPluginId) {
    for (AuthorizationProvider authProvider : context.getBeansOfType(AuthorizationProvider.class).values()) {
      if (authProvider.getPluginId().equals(requestPluginId)) {
        return authProvider;
      }
    }

    return null;
  }
}
