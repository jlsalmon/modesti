/**
 *
 */
package cern.modesti.workflow;

import java.io.IOException;
import java.util.*;

import javax.transaction.Transactional;

import cern.modesti.notification.NotificationService;
import cern.modesti.notification.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

  @Autowired
  NotificationService notificationService;

  /**
   *
   * @param request
   */
  public void startProcessInstance(final Request request) {
    LOG.info("starting process for request " + request.getRequestId());
    request.setStatus(Request.RequestStatus.IN_PROGRESS);

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());

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

    boolean approvalRequired = false;

    if (request.requiresApproval()) {
      // Dirty check: if all the points are clean and have been approved already,
      // then we don't need to approve again.

      for (Point point : request.getPoints()) {

        // If there is a single dirty or unapproved point, approval is required
        if (point.isDirty() || !point.isApproved()) {
          approvalRequired = true;
          break;
        }
      }
    }

    // If approval is required, notify the original requestor and the approval team.
    if (approvalRequired) {
      notificationService.sendNotification(request, NotificationType.APPROVAL_STARTED);
      notificationService.sendNotification(request, NotificationType.NEW_REQUEST_FOR_APPROVAL);
    }

    return approvalRequired;
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

    boolean requiresCabling = false;

    if (request.requiresCabling()) {
      requiresCabling = true;
    }

    // Send appropriate notifications
    if (requiresCabling) {
      if (request.getStatus().equals(Request.RequestStatus.FOR_ADDRESSING)) {
        notificationService.sendNotification(request, NotificationType.ADDRESSING_STARTED);
        notificationService.sendNotification(request, NotificationType.NEW_REQUEST_FOR_ADDRESSING);
      }

      else if (request.getStatus().equals(Request.RequestStatus.FOR_CABLING)) {
        notificationService.sendNotification(request, NotificationType.CABLING_STARTED);
        notificationService.sendNotification(request, NotificationType.NEW_REQUEST_FOR_CABLING);
      }
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


    // TODO: add dirty check: don't need to validate if all the points have already been validated


    /**
     * TODO: perform actual request validation here
     */


    // Randomly fail the validation
    boolean failed = new Random(System.currentTimeMillis()).nextBoolean();

    if (failed) {
      request.setValidationResult(new ValidationResult(true));
    } else {
      request.setValidationResult(new ValidationResult(false));

      // Mark all points as clean
      for (Point point : request.getPoints()) {
        point.setDirty(false);
      }
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
  public void onApprovalCompleted(String requestId, DelegateExecution execution) throws IOException {
    LOG.info("processing approval result for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    // We will have gotten a JSON serialised representation of an ApprovalResult from the user task.
    String approvalResultString = execution.getVariable("approvalResult", String.class);

    ApprovalResult approvalResult = new ObjectMapper().readValue(approvalResultString, ApprovalResult.class);
    List<Point> points = request.getPoints();

    request.setApprovalResult(approvalResult);

    // Mark all the points as approved or not
    for (Point point : request.getPoints()) {

      for (ApprovalResult.ApprovalResultItem item : approvalResult.getItems()) {
        if (Objects.equals(item.getId(), point.getId())) {
          point.setApproved(item.isApproved());
        }
      }
    }

    // Send an email to the original requestor
    notificationService.sendNotification(request, NotificationType.APPROVAL_COMPLETED);

    // Set the variable for the next stage to evaluate
    execution.setVariable("approved", approvalResult.isApproved());

    // Store the request
    requestRepository.save(request);
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void onAddressingCompleted(String requestId, DelegateExecution execution) {
    LOG.info("processing addressing result for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    // We will have gotten a JSON serialised representation of an AddressingResult from the user task.
    String addressingResultString = execution.getVariable("addressingResult", String.class);

    AddressingResult addressingResult = new Gson().fromJson(addressingResultString, AddressingResult.class);
    request.setAddressingResult(addressingResult);

    // Set the variable for the next stage to evaluate
    execution.setVariable("addressed", addressingResult.isAddressed());

    // Store the request
    requestRepository.save(request);
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void onCablingCompleted(String requestId, DelegateExecution execution) {
    LOG.info("processing cabling result for request id " + requestId + "...");

    // Nothing to do here yet
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void onTestingCompleted(String requestId, DelegateExecution execution) {
    LOG.info("processing testing result for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    // We will have gotten a JSON serialised representation of a TestResult from the user task.
    String testResultString = execution.getVariable("testResult", String.class);

    TestResult testResult = new Gson().fromJson(testResultString, TestResult.class);
    request.setTestResult(testResult);

    // Set the variable for the next stage to evaluate
    execution.setVariable("passed", testResult.getPassed());

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

    // Store the request
    requestRepository.save(request);
  }

  /**
   *
   * @param requestId
   * @param execution
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
