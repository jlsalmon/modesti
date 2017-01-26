package cern.modesti.request;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.counter.CounterService;
import cern.modesti.request.history.RequestHistoryService;
import cern.modesti.point.Point;
import cern.modesti.point.PointImpl;
import cern.modesti.request.history.RequestHistoryServiceImpl;
import cern.modesti.request.spi.RequestEventHandler;
import cern.modesti.user.User;
import cern.modesti.security.UserService;
import cern.modesti.workflow.AuthService;
import cern.modesti.workflow.CoreWorkflowService;
import cern.modesti.workflow.task.NotAuthorisedException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Service("requestService")
@Slf4j
public class RequestServiceImpl implements RequestService {

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
   * Insert (create) a new request.
   * <p>
   * Creating a new request performs the following actions:
   * <ul>
   * <li>
   * Asserts that the currently logged-in user is authorised to create a
   * request for the domain of the request
   * </li>
   * <li>
   * Sets the currently logged-in user as the creator of the request
   * </li>
   * <li>Generates a request id</li>
   * <li>Adds some empty points to the request if none were specified</li>
   * <li>Starts a new workflow process instance using the workflow key of the
   * plugin associated with the request domain</li>
   * </ul>
   *
   * @param request the request to create
   * @return the newly created request with all properties set
   */
  public Request insert(Request request) {
    // Do not create a request if there is no appropriate domain
    RequestProvider plugin = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));
    User user = userService.getCurrentUser();

    // Assert that the current user is allowed to create a request for this domain
    if (!authService.canCreate(plugin, request, user)) {
      throw new NotAuthorisedException(format("User \"%s\" is not authorised to create requests for domain \"%s\". " +
          "Authorisation group is \"%s\".", user.getUsername(), request.getDomain(), plugin.getMetadata().getAuthorisationGroup(request)));
    }

    // Set the creator as the current logged in user
    request.setCreator(user.getUsername());

    ((RequestImpl) request).setRequestId(counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString());
    log.trace(format("generated request id: %s", request.getRequestId()));

    ((RequestImpl) request).setCreatedAt(new DateTime());

    if (request.getPoints() == null) {
      request.setPoints(new ArrayList<>());
    }

    // Add some empty points if there aren't any yet
    if (request.getPoints().isEmpty()) {
      for (int i = 0; i < 50; i++) {
        Point point = new PointImpl((long) (i + 1));
        request.addPoint(point);
      }
    }

    for (Point point : request.getPoints()) {
      if (point.getLineNo() == null) {
        point.setLineNo((long) (request.getPoints().indexOf(point) + 1));
      }
    }

    request = repository.save((RequestImpl) request);

    if (request.getType().equals(RequestType.UPDATE)) {
      // Store an initial, empty change history
      ((RequestHistoryServiceImpl) historyService).initialiseChangeHistory(request);
    }

    // Kick off the workflow process
    workflowService.startProcessInstance(request);

    return request;
  }

  /**
   * Save an existing request.
   *
   * @param updated the request to save
   * @return the newly saved request
   */
  public Request save(Request updated) {
    Request original = repository.findOne(updated.getId());
    if (original == null) {
      throw new RuntimeException(format("Request #%s was not found", updated.getId()));
    }

    // The request id may not be modified manually.
    if (!Objects.equals(updated.getRequestId(), original.getRequestId())) {
      throw new IllegalArgumentException("Request ID cannot not be updated manually!");
    }

    // The request status may not be modified manually.
    if (!Objects.equals(updated.getStatus(), original.getStatus())) {
      throw new IllegalArgumentException("Request status cannot not be updated manually!");
    }

    // TODO: this shouldn't be necessary, and could cause side effects. Why do we lose properties when saving?
    Map<String, Object> properties = original.getProperties();
    properties.putAll(updated.getProperties());
    updated.setProperties(properties);

    for (Point point : updated.getPoints()) {
      if (point.getLineNo() == null) {
        point.setLineNo((long) (updated.getPoints().indexOf(point) + 1));
      }
    }

    // Invoke any callbacks
    for (RequestEventHandler requestEventHandler : requestEventHandlers) {
      requestEventHandler.onBeforeSave(updated);
    }

    if (updated.getType().equals(RequestType.UPDATE)) {
      // Process and store any changes that were made to the request
      ((RequestHistoryServiceImpl) historyService).saveChangeHistory(updated);
    }

    return repository.save((RequestImpl) updated);
  }

  /**
   * Find a single request.
   *
   * @param requestId the id of the request
   * @return the request instance, or null if no request was found with the
   * given id
   */
  public Request findOneByRequestId(String requestId) {
    return repository.findOneByRequestId(requestId);
  }

  /**
   * Delete a request.
   *
   * @param request the request to delete
   */
  public void delete(Request request) {
    // TODO: mark the request as deleted in the history collection

    repository.delete((RequestImpl) request);
  }
}
