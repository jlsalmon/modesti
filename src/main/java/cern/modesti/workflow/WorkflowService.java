/**
 *
 */
package cern.modesti.workflow;

import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.modesti.configuration.ConfigurationService;
import cern.modesti.configuration.ProgressUpdateListener;
import cern.modesti.notification.NotificationService;
import cern.modesti.notification.NotificationType;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.request.Request;
import cern.modesti.request.point.state.Approval;
import cern.modesti.request.point.Point;
import cern.modesti.validation.ValidationService;
import cern.modesti.workflow.result.ConfigurationResult;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
@Transactional
public class WorkflowService {

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private CounterService counterService;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private ValidationService validationService;

  @Autowired
  private ConfigurationService configurationService;

  /**
   * @param request
   */
  public void startProcessInstance(final Request request) {
    log.info("starting process for request " + request.getRequestId());
    request.setStatus(Request.RequestStatus.IN_PROGRESS);

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());

    runtimeService.startProcessInstanceByKey("create-tim-points", request.getRequestId(), variables);
  }

  /**
   * Set the business key for a process instance.
   *
   * This method is invoked by an execution listener when the process starts in order to set the business key (request id) that will be used throughout the
   * rest of the workflow. Although we can set the business key via {@link RuntimeService#startProcessInstanceById(String, String)} when starting a new
   * parent process, we are not responsible for starting process instances of subprocess call activities, and Activiti currently does not support passing a
   * business key directly to a call activity. So we use this method to ensure that child activities have their business keys set correctly.
   *
   * @param requestId
   * @param execution
   */
  public void setBusinessKey(String requestId, DelegateExecution execution) {
    runtimeService.updateBusinessKey(execution.getProcessInstanceId(), requestId);
  }

  /**
   * @param requestId
   * @param status
   */
  public void setRequestStatus(String requestId, String status) {
    log.info("setting status " + status + " on request id " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    request.setStatus(Request.RequestStatus.valueOf(status));
    requestRepository.save(request);
  }

  /**
   * @param requestId
   *
   * @return
   */
  public boolean requiresApproval(String requestId) {
    log.info("determining approval requirement for request " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    boolean approvalRequired = false;

    if (request.requiresApproval()) {
      // Dirty check: if all the points are clean and have been approved already,
      // then we don't need to approve again.
      for (Point point : request.getPoints()) {

        if (point.isAlarm()) {
          // If there is a single dirty or unapproved point, approval is required
          if (point.getDirty() || point.getApproval() == null || point.getApproval().getApproved() == null || !point.getApproval().getApproved()) {
            approvalRequired = true;
            break;
          }
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
   * @param requestId
   *
   * @return
   */
  public boolean requiresCabling(String requestId) {
    log.info("determining cabling requirement for request " + requestId);

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
      } else if (request.getStatus().equals(Request.RequestStatus.FOR_CABLING)) {
        notificationService.sendNotification(request, NotificationType.CABLING_STARTED);
        notificationService.sendNotification(request, NotificationType.NEW_REQUEST_FOR_CABLING);
      }
    }

    return request.requiresCabling();
  }

  /**
   * @param requestId
   * @param execution
   */
  public void validateRequest(String requestId, DelegateExecution execution) {
    log.info("validating request " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }


    // TODO: add dirty check: don't need to validate if all the points have already been validated
    //boolean valid = validationService.validateRequest(request);
    boolean valid = validationService.validateRequest(request);

    if (valid) {
      for (Point point : request.getPoints()) {

        if (point.getApproval() != null && point.getApproval().getApproved() != null && point.getDirty()) {
          // If a point is dirty and has already been approved, it will need re-approval
          point.setApproval(new Approval());

        } else {
          // Mark point as clean
          point.setDirty(false);
        }
      }
    }

    // Set the variable for the next stage to evaluate
    execution.setVariable("containsErrors", !valid);

    // Store the request
    requestRepository.save(request);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void onApprovalCompleted(String requestId, DelegateExecution execution) throws IOException {
    log.info("processing approval result for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    // Send an email to the original requestor
    notificationService.sendNotification(request, NotificationType.APPROVAL_COMPLETED);

    // Set the variable for the next stage to evaluate
    execution.setVariable("approved", request.getApproval().getApproved());

    // Store the request
    requestRepository.save(request);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void onAddressingCompleted(String requestId, DelegateExecution execution) {
    log.info("processing addressing result for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    // Set the variable for the next stage to evaluate
    execution.setVariable("addressed", request.getAddressing().getAddressed());

    // Store the request
    requestRepository.save(request);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void onCablingCompleted(String requestId, DelegateExecution execution) {
    log.info("processing cabling result for request id " + requestId + "...");

    // Nothing to do here yet
  }

  /**
   * @param requestId
   * @param execution
   */
  public void onTestingCompleted(String requestId, DelegateExecution execution) {
    log.info("processing testing result for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    // Set the variable for the next stage to evaluate
    execution.setVariable("passed", request.getTesting().getTested());

    // Store the request
    requestRepository.save(request);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void configureRequest(String requestId, DelegateExecution execution) {
    log.info("configuring points for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }


    /**
     * TODO: implement actual point configuration here
     */

    ConfigurationReport report = configurationService.configureRequest(request, new ProgressUpdateListener());

    // OK, WARNING and RESTART are all considered successful
    boolean failure = report.getStatus() == ConfigConstants.Status.FAILURE;
    ConfigurationResult result;

    if (failure) {
      result = new ConfigurationResult(false);
      result.setErrors(Collections.singletonList(report.getStatusDescription()));
    } else {
      result = new ConfigurationResult(true);
    }

    request.setConfigurationResult(result);
    execution.setVariable("configurationFailure", failure);

    // Store the request
    requestRepository.save(request);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void splitRequest(String requestId, DelegateExecution execution) {
    log.info(format("splitting request id %s...", requestId));

    List<Long> pointsToSplit = execution.getVariable("points", List.class);
    log.info(format("splitting points [%s]", StringUtils.join(pointsToSplit, ", ")));

    Request parent = requestRepository.findOneByRequestId(requestId);
    if (parent == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }


    List<Point> childPoints = new ArrayList<>();

    // Give the split points to the child.
    for (Long pointId : pointsToSplit) {
      Point pointToSplit = null;

      for (Point point : parent.getPoints()) {
        if (point.getId().equals(pointId)) {
          pointToSplit = point;
          break;
        }
      }

      if (pointToSplit != null) {
        childPoints.add(pointToSplit);
        parent.getPoints().remove(pointToSplit);
        pointToSplit.setId((long) (childPoints.indexOf(pointToSplit) + 1));
      }
    }

    // Rebase the point IDs back to starting from 1.
    for (Point point : parent.getPoints()) {
      //if (pointIdsToSplit.contains(point.getId())) {
      point.setId((long) (parent.getPoints().indexOf(point) + 1));
      //}
    }

    // Generate a request ID for the new child
    String childRequestId = counterService.getNextSequence(CounterService.REQUEST_ID_SEQUENCE).toString();

    // Create the new child
    Request child = createChildRequest(childRequestId, parent, childPoints);

    // Set back reference to the child
    parent.getChildRequestIds().add(childRequestId);

    // Store the requests
    requestRepository.save(parent);
    requestRepository.save(child);

    // Add variables to the execution so that they are available to the
    // recursive process invocation
    execution.setVariable("childRequestId", child.getRequestId());
    //    execution.setVariable("childRequiresApproval", child.requiresApproval());
    //    execution.setVariable("childRequiresCabling", child.requiresCabling());
  }

  /**
   * @param requestId
   * @param parent
   * @param points
   *
   * @return
   */
  private Request createChildRequest(String requestId, Request parent, List<Point> points) {
    Request request = new Request(parent);
    request.setRequestId(requestId);
    request.setParentRequestId(parent.getRequestId());
    request.setChildRequestIds(new ArrayList<>());
    request.setPoints(points);
    return request;
  }
}
