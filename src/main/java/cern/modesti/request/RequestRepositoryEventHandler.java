package cern.modesti.request;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.counter.CounterService;
import cern.modesti.request.point.Point;
import cern.modesti.workflow.CoreWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.String.format;

/**
 * TODO
 *
 * The {@link cern.modesti.request.RequestRepository} is automatically exposed as a REST resource via Spring Data REST, hence why there is no explicit MVC
 * controller for it. This class simply hooks into the Spring Data REST lifecycle and intercepts request create/save events, and lets Spring Data REST do
 * everything else automatically.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
@RepositoryEventHandler(Request.class)
public class RequestRepositoryEventHandler {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private CounterService counterService;

  @Autowired
  private CoreWorkflowService workflowService;

  @Autowired
  private ApplicationContext context;

  private Collection<RequestEventHandler> requestEventHandlers = new ArrayList<>();

  /**
   *
   */
  @PostConstruct
  public void init() {
    // TODO: associate event handlers of a domain to requests of that domain only
    this.requestEventHandlers = context.getBeansOfType(RequestEventHandler.class).values();
  }

  /**
   * TODO
   *
   * @param request
   */
  @HandleBeforeCreate
  public void handleRequestCreate(Request request) {
    // Do not create a request if there is no appropriate domain
    if (!requestProviderRegistry.hasPluginFor(request)) {
      throw new UnsupportedRequestException(request);
    }

    request.setRequestId(counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString());
    log.trace(format("generated request id: %s", request.getRequestId()));

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

    // Kick off the workflow process
    workflowService.startProcessInstance(request);
  }

  /**
   * TODO
   *
   * @param request
   */
  @HandleBeforeSave
  public void handleRequestSave(Request request) {
    for (Point point : request.getPoints()) {
      if (point.getLineNo() == null) {
        point.setLineNo((long) (request.getPoints().indexOf(point) + 1));
      }
    }

    // Invoke any callbacks
    for (RequestEventHandler requestEventHandler : requestEventHandlers) {
      requestEventHandler.onBeforeSave(request);
    }
  }
}