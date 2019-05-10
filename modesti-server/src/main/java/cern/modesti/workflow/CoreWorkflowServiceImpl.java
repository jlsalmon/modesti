package cern.modesti.workflow;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.counter.CounterService;
import cern.modesti.point.Point;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Service class providing core functionality for use within workflows.
 * <p>
 * The public methods in this service can be invoked directly from expressions.
 * For example:
 * <p>
 * <code>
 * &lt;activiti:executionListener event="start" expression="${coreWorkflowService.setRequestStatus(requestId, 'FOR_ADDRESSING')}" /&gt;
 * </code>
 *
 * @author Justin Lewis Salmon
 */
@Service("coreWorkflowService")
@Slf4j
@Transactional
public class CoreWorkflowServiceImpl implements CoreWorkflowService {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private CounterService counterService;

  @Autowired
  private RuntimeService runtimeService;

  /**
   * Start a new workflow process instance for the given request.
   *
   * @param request the request to be associated with the newly created
   *                workflow process instance
   * @return the newly started process instance object
   */
  public ProcessInstance startProcessInstance(final Request request) {
    log.info(format("starting process for %s request %s", request.getDomain(), request.getRequestId()));

    // Figure out which process to start, based on the domain and type
    RequestProvider plugin = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));
    String processKey = plugin.getMetadata().getProcessKey(request.getType());

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    variables.put("creator", request.getCreator());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, request.getRequestId(), variables);
    
    // After initializing the process instance, sets the request status (it might have been modified by some Activiti tasks)
    Request savedRequest = getRequest(request.getRequestId());
    request.setStatus(savedRequest.getStatus());
    request.setErrors(savedRequest.getErrors());
    request.setPoints(savedRequest.getPoints());
    request.setSkipCoreValidation(savedRequest.isSkipCoreValidation());
    
    return processInstance;
  }

  /**
   * Set the business key for a process instance.
   * <p>
   * This method is invoked by an execution listener when the process starts in
   * order to set the business key (request id) that will be used throughout
   * the rest of the workflow. Although we can set the business key via
   * {@link RuntimeService#startProcessInstanceById(String, String)} when
   * starting a new* parent process, we are not responsible for starting
   * process instances of subprocess call activities, and Activiti currently
   * does not support passing a business key directly to a call activity. So
   * we use this method to ensure that child activities have their business
   * keys set correctly.
   *
   * @param requestId the id of the request
   * @param execution the Activiti execution object
   */
  public void setBusinessKey(String requestId, DelegateExecution execution) {
    runtimeService.updateBusinessKey(execution.getProcessInstanceId(), requestId);
  }

  /**
   * Retrieve the workflow process instance object associated with a particular
   * request.
   *
   * @param requestId the id of the request
   * @return the {@link ProcessInstance} object associated with the request
   */
  public ProcessInstance getProcessInstance(String requestId) {
    return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(requestId).singleResult();
  }

  /**
   * Update the status of a particular request.
   * <p>
   * A status is simply a string. Conventionally, it should be in
   * {@literal UPPER_UNDERSCORE} format, e.g. {@literal IN_PROGRESS}.
   *
   * @param requestId the id of the request to update
   * @param status    the new workflow status string
   */
  public void setRequestStatus(String requestId, String status) {
    log.info(format("setting status %s on request id %s", status, requestId));
    Request request = getRequest(requestId);

    request.setStatus(status);
    requestRepository.save((RequestImpl) request);
  }

  /**
   * Split a set of points from a request into a child request.
   * <p>
   * The points to split must be specified in an execution variable named
   * {@literal points} which is a list of line numbers corresponding to the
   * lines to be split.
   *
   * @param requestId the id of the request to split
   * @param execution the Activiti execution object
   */
  public void splitRequest(String requestId, DelegateExecution execution) {
    log.info(format("splitting request id %s...", requestId));

    List<Long> pointsToSplit = execution.getVariable("points", List.class);
    log.info(format("splitting points [%s]", StringUtils.join(pointsToSplit, ", ")));

    Request parent = getRequest(requestId);
    List<Point> childPoints = new ArrayList<>();

    // Give the split points to the child.
    for (Long lineNo : pointsToSplit) {
      Point pointToSplit = null;

      for (Point point : parent.getPoints()) {
        if (point.getLineNo().equals(lineNo)) {
          pointToSplit = point;
          break;
        }
      }

      if (pointToSplit != null) {
        childPoints.add(pointToSplit);
        parent.getPoints().remove(pointToSplit);
        pointToSplit.setLineNo((long) (childPoints.indexOf(pointToSplit) + 1));
      }
    }

    // Rebase the point IDs back to starting from 1.
    for (Point point : parent.getPoints()) {
      point.setLineNo((long) (parent.getPoints().indexOf(point) + 1));
    }

    // Generate a request ID for the new child
    String childRequestId = counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString();

    // Create the new child
    Request child = createChildRequest(childRequestId, parent, childPoints);

    // Set back reference to the child
    parent.getChildRequestIds().add(childRequestId);

    // Store the requests
    requestRepository.save((RequestImpl) parent);
    requestRepository.save((RequestImpl) child);

    // Add variables to the execution so that they are available to the
    // recursive process invocation
    execution.setVariable("childRequestId", child.getRequestId());
    execution.setVariable("childCreator", child.getCreator());
  }

  private Request getRequest(String requestId) {
    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new RuntimeException("No request with id " + requestId + " was found");
    }
    return request;
  }

  private Request createChildRequest(String requestId, Request parent, List<Point> points) {
    RequestImpl request = new RequestImpl((RequestImpl) parent);
    request.setRequestId(requestId);
    request.setParentRequestId(parent.getRequestId());
    request.setChildRequestIds(new ArrayList<>());
    request.setPoints(points);
    return request;
  }
}
