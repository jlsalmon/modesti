/**
 *
 */
package cern.modesti.workflow;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.counter.CounterService;
import cern.modesti.request.point.Point;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
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
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
@Transactional
public class CoreWorkflowService {

  private static final String DEFAULT_INITIAL_STATUS = "IN_PROGRESS";

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

  /**
   * @param request
   */
  public ProcessInstance startProcessInstance(final Request request) {
    log.info(format("starting process for %s request %s", request.getDomain(), request.getRequestId()));

    // Figure out which process to start, based on the domain and type
    RequestProvider plugin = requestProviderRegistry.getPluginFor(request, new UnsupportedRequestException(request));
    String processKey = plugin.getMetadata().getProcessKey(request.getType());

    request.setStatus(DEFAULT_INITIAL_STATUS);
    request.setAssignee(request.getCreator());
    requestRepository.save(request);

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    variables.put("creator", request.getCreator().getUsername());

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

    request.setStatus(status);
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
      //if (pointIdsToSplit.contains(point.getId())) {
      point.setLineNo((long) (parent.getPoints().indexOf(point) + 1));
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
