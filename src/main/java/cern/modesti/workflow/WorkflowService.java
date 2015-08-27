/**
 *
 */
package cern.modesti.workflow;

import cern.modesti.notification.NotificationService;
import cern.modesti.notification.NotificationType;
import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.RequestStatus;
import cern.modesti.request.counter.CounterService;
import cern.modesti.request.point.Point;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
@Transactional
public class WorkflowService {

  private static final String PROCESS_RESOURCE_PATTERN = "classpath*:/processes/*.bpmn20.xml";

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private CounterService counterService;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private TaskService taskService;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private NotificationService notificationService;

  /**
   *
   * @throws IOException
   */
  @PostConstruct
  public void init() throws IOException {
    log.info("Initialising workflow processes");

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource[] processes = resolver.getResources(PROCESS_RESOURCE_PATTERN);

    for (Resource process : processes) {
      repositoryService.createDeployment().addInputStream(process.getFilename(), process.getInputStream()).deploy();
    }
  }

  /**
   * @param request
   */
  public ProcessInstance startProcessInstance(final Request request) {
    log.info(format("starting process for %s request %s", request.getDomain(), request.getRequestId()));

    request.setStatus(RequestStatus.IN_PROGRESS);
    requestRepository.save(request);

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    variables.put("creator", request.getCreator().getUsername());

    // Figure out which process to start, based on the domain and type
    RequestProvider provider = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));
    String processKey = provider.getMetadata().getProcessKey(request.getType());

    return runtimeService.startProcessInstanceByKey(processKey, request.getRequestId(), variables);
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
    log.info(format("setting status %s on request id %s", status, requestId));
    Request request = getRequest(requestId);

    request.setStatus(RequestStatus.valueOf(status));
    requestRepository.save(request);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void validateRequest(String requestId, DelegateExecution execution) {
    log.info("validating request " + requestId);
    Request request = getRequest(requestId);

    RequestProvider provider = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));

    boolean valid = provider.validate(request);


    // TODO: add dirty check: don't need to validate if all the points have already been validated


    // Set the variable for the next stage to evaluate
    execution.setVariable("valid", valid);

    // Store the request
    request.setValid(valid);
    requestRepository.save(request);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void determineApprovalRequirement(String requestId, DelegateExecution execution) {
    log.info("determining approval requirement for request " + requestId);
    Request request = getRequest(requestId);

    boolean requiresApproval = false;

    if (request.requiresApproval()) {
      // Dirty check: if all the points are clean and have been approved already,
      // then we don't need to approve again.
      for (Point point : request.getPoints()) {

        if (point.isAlarm()) {
          // If there is a single dirty or unapproved point, approval is required
          if (point.getDirty() || point.getApproval() == null || point.getApproval().getApproved() == null || !point.getApproval().getApproved()) {
            requiresApproval = true;
            break;
          }
        }
      }
    }

    execution.setVariable("requiresApproval", requiresApproval);
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void onApprovalStarted(String requestId, DelegateExecution execution) {
    log.debug(format("approval started for request %s", requestId));
    Request request = getRequest(requestId);

    // Notify the original requestor and the approval team.
    notificationService.sendNotification(request, NotificationType.APPROVAL_STARTED);
    notificationService.sendNotification(request, NotificationType.NEW_REQUEST_FOR_APPROVAL);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void onApprovalCompleted(String requestId, DelegateExecution execution) throws IOException {
    log.info(format("approval completed for request %s", requestId));
    Request request = getRequest(requestId);

    if (request.getApproval() == null || request.getApproval().getApproved() == null) {
      throw new ActivitiException("Request approval object must not be null!");
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
   */
  public void determineCablingRequirement(String requestId, DelegateExecution execution) {
    log.info("determining cabling requirement for request " + requestId);
    Request request = getRequest(requestId);

    // TODO: add dirty check here

    execution.setVariable("requiresCabling", request.requiresCabling());
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void onAddressingStarted(String requestId, DelegateExecution execution) {
    log.info(format("addressing started for request %s", requestId));
    Request request = getRequest(requestId);

    notificationService.sendNotification(request, NotificationType.ADDRESSING_STARTED);
    notificationService.sendNotification(request, NotificationType.NEW_REQUEST_FOR_ADDRESSING);
  }

  /**
   * @param requestId
   * @param execution
   */
  public void onAddressingCompleted(String requestId, DelegateExecution execution) {
    log.info("processing addressing result for request id " + requestId + "...");
    Request request = getRequest(requestId);

    if (request.getAddressing() == null || request.getAddressing().getAddressed() == null) {
      throw new ActivitiException("Request addressing object must not be null!");
    }

    // Set the variable for the next stage to evaluate
    execution.setVariable("addressed", request.getAddressing().getAddressed());

    // Store the request
    requestRepository.save(request);
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void onCablingStarted(String requestId, DelegateExecution execution) {
    log.info(format("cabling started for request %s", requestId));
    Request request = getRequest(requestId);

    notificationService.sendNotification(request, NotificationType.CABLING_STARTED);
    notificationService.sendNotification(request, NotificationType.NEW_REQUEST_FOR_CABLING);
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
    Request request = getRequest(requestId);

    if (request.getTesting() == null || request.getTesting().getTested() == null) {
      throw new ActivitiException("Request testing object must not be null!");
    }

    // Set the variable for the next stage to evaluate
    execution.setVariable("accepted", request.getTesting().getTested());

    // Store the request
    requestRepository.save(request);
  }

  /**
   *
   * @param requestId
   * @param execution
   */
  public void onTaskClaimed(String requestId, DelegateExecution execution) {
    // Place the assignee name as a variable "editor" in the execution. This is so that we can automatically assign the
    // "submit" task which inevitably follows. The sequence is always "edit" -> "validate" -> "submit".
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).taskName("edit").singleResult();
    execution.setVariable("editor", task.getAssignee());
  }

  /**
   * @param requestId
   * @param execution
   */
  public void splitRequest(String requestId, DelegateExecution execution) {
    log.info(format("splitting request id %s...", requestId));

    List<Long> pointsToSplit = execution.getVariable("points", List.class);
    log.info(format("splitting points [%s]", StringUtils.join(pointsToSplit, ", ")));

    Request parent = getRequest(requestId);
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
   *
   * @param requestId
   * @return
   */
  private Request getRequest(String requestId) {
    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }
    return request;
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
