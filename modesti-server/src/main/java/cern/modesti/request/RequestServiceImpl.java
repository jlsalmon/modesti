package cern.modesti.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
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
import cern.modesti.request.InvalidRequestException;
import cern.modesti.workflow.task.NotAuthorisedException;
import cern.modesti.workflow.validation.CoreValidationService;
import cern.modesti.point.Error;

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
  private CoreValidationService validationService;

  @Autowired
  private UserService userService;

  @Autowired
  private AuthService authService;

  @Autowired
  private ApplicationContext context;
  
  @Autowired
  private RequestFormatter requestFormatter;
  
  @Autowired
  private RequestDomainSearch requestDomainSearch;

  private Collection<RequestEventHandler> requestEventHandlers = new ArrayList<>();

  @PostConstruct
  public void init() {
    // TODO: associate event handlers of a domain to requests of that domain only
    this.requestEventHandlers = context.getBeansOfType(RequestEventHandler.class).values();
  }

  @Override
  public Request insert(Request request) {
    validateRequestDomain(request);
    User user = userService.getCurrentUser();
    // Set the creator as the current logged in user
    request.setCreator(user.getUsername());
    
    validateUserPermissions(request, user);
    preValidateRestRequest(request);

    ((RequestImpl) request).setRequestId(counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString());
    log.trace(format("generated request id: %s", request.getRequestId()));

    ((RequestImpl) request).setCreatedAt(new DateTime());

    if (request.getPoints() == null) {
      request.setPoints(new ArrayList<>());
    }
    
    removeSearchOnlyFields(request);
    // Apply formatting to the request points
    formatRequest(request);

    boolean isEmptyRequest = request.getPoints().isEmpty();
    if ((RequestType.UPDATE.equals(request.getType()) || RequestType.DELETE.equals(request.getType()))
        && isEmptyRequest) {
      // Update and delete request must contain non empty points
      throw new InvalidRequestException(format("%s requests must contain at least one point", request.getType()));
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

    Request newRequest = repository.save((RequestImpl) request);
    // Kick off the workflow process
    workflowService.startProcessInstance(newRequest);

    if (newRequest.getType().equals(RequestType.UPDATE)) {
      // Store an initial, empty change history
      ((RequestHistoryServiceImpl) historyService).initialiseChangeHistory(request);
    }

    if (newRequest.isGeneratedFromUi()) {
      // Initially updated/cloned requests are not valid (values in the database might be incorrect)
      newRequest.setValid(isEmptyRequest || newRequest.getType().equals(RequestType.DELETE));
      repository.save((RequestImpl) newRequest);
    }
    
    return newRequest;
  }

  private void formatRequest(Request request) {
    Optional<SchemaImpl> schemaOpt = schemaRepository.findById(request.getDomain());
    SchemaImpl schema = schemaOpt.isPresent() ? schemaOpt.get() : null;
    requestFormatter.format(request, schema);
  }
  
  private void preValidateRestRequest(Request request) {
    if (request.isGeneratedFromUi()) {
      return;
    }
    
    if (request.getNonEmptyPoints().size() > 500) {
      throw new InvalidRequestException(format("Pre-validation failed for the request: "
          + "Maximum number of points exceeded (500)"));
    }
    
    // If the request has not been created from the MODESTI UI it must be pre-validated
    if (!validationService.preValidateRequest(request)) {
      // Converts all the errors to a single String
      List<String> errorList = request.getNonEmptyPoints().stream()
          .map(Point::getErrors)
          .flatMap(Collection::stream)
          .map(Error::toString)
          .collect(Collectors.toList());
      String errorAsString = StringUtils.join(errorList, System.lineSeparator());
      throw new InvalidRequestException(format("Pre-validation failed for the request: %s", errorAsString));
    }
  }

  private void validateUserPermissions(Request request, User user) {
    // Do not create a request if there is no appropriate domain
    RequestProvider plugin = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));
    // Assert that the current user is allowed to create a request for this domain
    if (!authService.canCreate(plugin, request, user)) {
      throw new NotAuthorisedException(format("User \"%s\" is not authorised to create requests for domain \"%s\". " +
          "Authorisation group is \"%s\".", user.getUsername(), request.getDomain(), plugin.getMetadata().getAuthorisationGroup(request)));
    }
  }

  /**
   * REST requests might not have a domain specified for UPDATE requests (e.g. update requests from HelpAlarm).
   * In that case, it tries to find the domain for the request
   * @param request The original request.
   */
  private void validateRequestDomain(Request request) {
    if (!StringUtils.isEmpty(request.getDomain()) || RequestType.UPDATE != request.getType()) {
      return;
    }
    
    String domain = requestDomainSearch.find(request);
    request.setDomain(domain);
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
    formatRequest(updated);

    if (updated.getType().equals(RequestType.UPDATE)
        && ("IN_PROGRESS".equals(updated.getStatus()) || "FOR_ADDRESSING".equals(updated.getStatus()) || "IN_ERROR".equals(updated.getStatus()))) {
      // Process and store any changes that were made to the request
      ((RequestHistoryServiceImpl) historyService).saveChangeHistory(updated);
    }

    return repository.save((RequestImpl) updated);
  }

  @Override
  public Request findOneByRequestId(String requestId) {
    return repository.findOneByRequestId(requestId);
  }

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
