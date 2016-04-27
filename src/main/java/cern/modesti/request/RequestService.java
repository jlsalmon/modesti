package cern.modesti.request;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.counter.CounterService;
import cern.modesti.request.history.RequestHistoryService;
import cern.modesti.request.point.Point;
import cern.modesti.user.MockUserService;
import cern.modesti.user.User;
import cern.modesti.user.UserService;
import cern.modesti.workflow.AuthService;
import cern.modesti.workflow.CoreWorkflowService;
import cern.modesti.workflow.task.NotAuthorisedException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class RequestService {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private RequestRepository repository;

  @Autowired
  private RequestHistoryService historyService;

  @Autowired
  private CounterService counterService;

  @Autowired
  private CoreWorkflowService workflowService;

  @Autowired
  private UserService userService;

  @Autowired
  private AuthService authService;

  @Autowired
  private ApplicationContext context;

  private Collection<RequestEventHandler> requestEventHandlers = new ArrayList<>();

  @PostConstruct
  public void init() {
    // TODO: associate event handlers of a domain to requests of that domain only
    this.requestEventHandlers = context.getBeansOfType(RequestEventHandler.class).values();
  }

  /**
   *
   * @param request
   */
  public Request insert(Request request) {
    // Do not create a request if there is no appropriate domain
    RequestProvider plugin = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));
    User user = userService.getCurrentUser();

    // Assert that the current user is allowed to create a request for this domain
    if (!authService.isAuthorised(plugin, user)) {
      throw new NotAuthorisedException(format("User %s is not authorised to create requests for domain %s", user.getUsername(), request.getDomain()));
    }

    // Set the creator as the current logged in user
    request.setCreator(user.getUsername());

    request.setRequestId(counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString());
    log.trace(format("generated request id: %s", request.getRequestId()));

    request.setCreatedAt(new DateTime());

    // Add some empty points if there aren't any yet
    if (request.getPoints().isEmpty()) {
      for (int i = 0; i < 50; i++) {
        Point point = new Point((long) (i + 1));
        request.getPoints().add(point);
      }
    }

    for (Point point : request.getPoints()) {
      if (point.getLineNo() == null) {
        point.setLineNo((long) (request.getPoints().indexOf(point) + 1));
      }
    }

    request = repository.save(request);

    // Store an initial, empty change history
    historyService.initialiseChangeHistory(request);

    // Kick off the workflow process
    workflowService.startProcessInstance(request);

    return request;
  }

  /**
   *
   * @param request
   */
  public Request save(Request request) {
    if (repository.findOneByRequestId(request.getRequestId()) == null) {
      throw new RuntimeException(format("Request #%s was not found", request.getRequestId()));
    }

    for (Point point : request.getPoints()) {
      if (point.getLineNo() == null) {
        point.setLineNo((long) (request.getPoints().indexOf(point) + 1));
      }
    }

    // Invoke any callbacks
    for (RequestEventHandler requestEventHandler : requestEventHandlers) {
      requestEventHandler.onBeforeSave(request);
    }

    // Process and store any changes that were made to the request
    // historyService.saveChangeHistory(request);

    return repository.save(request);
  }

  public Request findOneByRequestId(String requestId) {
    return repository.findOneByRequestId(requestId);
  }

  /**
   *
   * @param request
   */
  public void delete(Request request) {
    // TODO: mark the request as deleted in the history collection

    repository.delete(request);
  }
}
