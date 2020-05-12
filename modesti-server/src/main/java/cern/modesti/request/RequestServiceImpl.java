package cern.modesti.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.point.Point;
import cern.modesti.point.PointImpl;
import cern.modesti.request.counter.CounterService;
import cern.modesti.request.history.RequestHistoryService;
import cern.modesti.request.history.RequestHistoryServiceImpl;
import cern.modesti.request.spi.RequestEventHandler;
import cern.modesti.schema.SchemaImpl;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.field.Field;
import cern.modesti.security.UserService;
import cern.modesti.user.User;
import cern.modesti.workflow.AuthService;
import cern.modesti.workflow.CoreWorkflowService;
import cern.modesti.workflow.request.RequestAction;
import cern.modesti.workflow.task.NotAuthorisedException;

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
  private SchemaRepository schemaRepository;

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
  
  @Autowired
  private RequestFormatter requestFormatter;

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
  @Override
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
    
    removeSearchOnlyFields(request);
    // Apply formatting to the request points
    requestFormatter.format(request);

    boolean isEmptyRequest = request.getPoints().isEmpty();
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

    Request newRequest = repository.save((RequestImpl) request);
    // Kick off the workflow process
    workflowService.startProcessInstance(newRequest);

    if (newRequest.getType().equals(RequestType.UPDATE)) {
      // Store an initial, empty change history
      ((RequestHistoryServiceImpl) historyService).initialiseChangeHistory(request);
    }

    // Checks the schema configuration to see if it allows create requests from the UI.
    // This is a (not too good) way to distinguish between PSEN and the other plug-ins.
    // TODO: Must be modified when the REST service to create requests is implemented!!!
    Optional<SchemaImpl> schema = schemaRepository.findById(request.getDomain());
    if (schema.isPresent() && (schema.get().getConfiguration() == null || schema.get().getConfiguration().isCreateFromUi())) {
      // Initially updated/cloned requests are not valid (values in the database might be incorrect)
      request.setValid(isEmptyRequest || request.getType().equals(RequestType.DELETE));
    }
    
    return newRequest;
  }

  private void removeSearchOnlyFields(Request request) {
    Optional<SchemaImpl> schemaOpt = schemaRepository.findById(request.getDomain());
    if(schemaOpt.isPresent()) {
      SchemaImpl schema = schemaOpt.get();
      // Concatenate all categories and datasources
      List<Category> categories = new ArrayList<>(schema.getCategories());
      categories.addAll(schema.getDatasources());
      for (Category category : categories) {
        for (Field field : category.getFields()) {
          if(Boolean.TRUE == field.getSearchFieldOnly()) {
            for (Point p : request.getNonEmptyPoints()) {
              p.getProperties().remove(field.getId());
            }
          }
        }
      }
    }
  }

  /**
   * Save an existing request.
   *
   * @param updated the request to save
   * @return the newly saved request
   */
  @Override
  public Request save(Request updated) {
    Optional<RequestImpl> originalOpt = repository.findById(updated.getId());
    if (!originalOpt.isPresent()) {
      throw new RuntimeException(format("Request #%s was not found", updated.getId()));
    }

    Request original = originalOpt.get();
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
    
    // Apply formatting to the request points
    requestFormatter.format(updated);

    if (updated.getType().equals(RequestType.UPDATE)
        && ("IN_PROGRESS".equals(updated.getStatus()) || "FOR_ADDRESSING".equals(updated.getStatus()) || "IN_ERROR".equals(updated.getStatus()))) {
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
  @Override
  public Request findOneByRequestId(String requestId) {
    return repository.findOneByRequestId(requestId);
  }

  /**
   * Delete a request.
   *
   * @param request the request to delete
   */
  @Override
  public void delete(Request request) {
    // TODO: mark the request as deleted in the history collection

    repository.delete((RequestImpl) request);
  }
  
  @Override
  public Request execute(String requestId, RequestAction action, User user) {
    if (RequestAction.Action.CREATOR.equals(action.getAction())) {
      return assignCreator(requestId, action.getCreator(), user.getUsername());
    } else {
      throw new UnsupportedOperationException(format("'%s' is not a valid action", action.getAction()));
    }
  }

  private Request assignCreator(String requestId, String newCreator, String originalCreator) {
    Request request = findOneByRequestId(requestId);
    if (request == null) {
      throw new IllegalArgumentException("No request with id " + requestId + " was found");
    }
    
    User newCreatorUser = userService.findOneByUsername(newCreator);
    if (newCreatorUser == null) {
      throw new IllegalArgumentException("No user with username " + newCreator + " was found");
    }
    
    if (!request.getCreator().equals(originalCreator)) {
      throw new NotAuthorisedException(format("User %s is not authorized to change the request creator", originalCreator));
    }
    
    request.setCreator(newCreator);
    save(request);
    
    return request;
  } 

}