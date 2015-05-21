/**
 *
 */
package cern.modesti.workflow;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * @author Justin Lewis Salmon
 *
 */
@Service
@Transactional
public class WorkflowService {
  private static final Logger LOG = LoggerFactory.getLogger(WorkflowService.class);

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private CounterService counterService;

  @Autowired
  private RuntimeService runtimeService;

  /**
   *
   * @param request
   */
  public void startProcessInstance(final Request request) {
    LOG.info("starting process for request " + request.getRequestId());
    request.setStatus(Request.RequestStatus.IN_PROGRESS);

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
//    variables.put("requiresApproval", request.requiresApproval());
//    variables.put("requiresCabling", request.requiresCabling());

    runtimeService.startProcessInstanceByKey("create-tim-points", request.getRequestId(), variables);
  }

  /**
   * @param requestId
   * @param status
   */
  public void setRequestStatus(String requestId, String status) {
    LOG.info("setting status " + status + " on request id " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    request.setStatus(Request.RequestStatus.valueOf(status));
    requestRepository.save(request);
  }

  /**
   *
   * @param requestId
   * @return
   */
  public boolean requiresApproval(String requestId) {
    LOG.info("determining approval requirement for request " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    return request.requiresApproval();
  }

  /**
   *
   * @param requestId
   * @return
   */
  public boolean requiresCabling(String requestId) {
    LOG.info("determining cabling requirement for request " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    return request.requiresCabling();
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void validateRequest(String requestId, DelegateExecution execution) {
    LOG.info("validating request " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }


    /**
     * TODO: perform actual request validation here
     */


    // Randomly fail the validation
    boolean failed = new Random(System.currentTimeMillis()).nextBoolean();

    if (failed) {
      request.setValidationResult(new ValidationResult(true));
    } else {
      request.setValidationResult(new ValidationResult(false));
    }

    // Set the variable for the next stage to evaluate
    execution.setVariable("containsErrors", failed);

    // Store the request
    requestRepository.save(request);
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void configureRequest(String requestId, DelegateExecution execution) {
    LOG.info("configuring points for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }


    /**
     * TODO: implement actual point configuration here
     */


    // Randomly fail the configuration
    boolean failed = new Random(System.currentTimeMillis()).nextBoolean();

    if (failed) {
      request.setConfigurationResult(new ConfigurationResult(true));
    } else {
      request.setConfigurationResult(new ConfigurationResult(false));
    }

    execution.setVariable("configurationFailure", failed);
  }

  /**
   *
   * @param requestId
   * @param execution
   * @throws Exception
   */
  public void splitRequest(String requestId, DelegateExecution execution) {
    LOG.info("splitting request id " + requestId + "...");

    String pointsToSplit = execution.getVariable("points", String.class);
    LOG.info("splitting points " + pointsToSplit);

    Request parent = requestRepository.findOneByRequestId(requestId);
    if (parent == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    // Parse the JSON list to a Java object
    Set<Long> pointIdsToSplit = new Gson().fromJson(pointsToSplit, new TypeToken<Set<Long>>() {}.getType());

    List<Point> childPoints = new ArrayList<>();

    // Give the split points to the child. Rebase the point IDs back to starting from 1.
    for (Point point : parent.getPoints()) {
      if (pointIdsToSplit.contains(point.getId())) {
        childPoints.add(point);
        point.setId((long) (childPoints.indexOf(point) + 1));
      }
    }

    // Generate a request ID for the new child
    String childRequestId = counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString();

    // Create the new child
    Request child = createChildRequest(childRequestId, parent, childPoints);

    // Set back reference to the child
    parent.setChildRequestIds(Collections.singletonList(childRequestId));

    // Store the requests
    requestRepository.save(parent);
    requestRepository.insert(child);

    // Add variables to the execution so that they are available to the
    // recursive process invocation
    execution.setVariable("childRequestId", child.getRequestId());
//    execution.setVariable("childRequiresApproval", child.requiresApproval());
//    execution.setVariable("childRequiresCabling", child.requiresCabling());
  }

  /**
   *
   * @param requestId
   * @param parent
   * @param points
   * @return
   */
  private Request createChildRequest(String requestId, Request parent, List<Point> points) {
    Request request = new Request(parent);
    request.setRequestId(requestId);
    request.setParentRequestId(parent.getRequestId());
    request.setPoints(points);
    return request;
  }
}
