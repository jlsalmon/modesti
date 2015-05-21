package cern.modesti.workflow.task;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Transactional
public class RequestSplittingTask {

  private static final Logger LOG = LoggerFactory.getLogger(RequestConfigurationTask.class);

  @Autowired
  RequestRepository requestRepository;

  @Autowired
  private CounterService counterService;

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
    execution.setVariable("childRequiresApproval", child.requiresApproval());
    execution.setVariable("childRequiresCabling", child.requiresCabling());
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
