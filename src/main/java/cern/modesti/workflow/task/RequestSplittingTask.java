package cern.modesti.workflow.task;

import cern.modesti.repository.mongo.request.RequestRepository;
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

    List<Point> child1Points = new ArrayList<>();
    List<Point> child2Points = new ArrayList<>();

    // Decide which children get which points. Rebase the point IDs back to starting from 1.
    for (Point point : parent.getPoints()) {
      if (pointIdsToSplit.contains(point.getId())) {
        child1Points.add(point);
        point.setId((long) (child1Points.indexOf(point) + 1));
      } else {
        child2Points.add(point);
        point.setId((long) (child2Points.indexOf(point) + 1));
      }
    }

    // Figure out request IDs for the new children
    String child1RequestId = parent.getRequestId() + "a";
    String child2RequestId = parent.getRequestId() + "b";

    // Create the new children
    Request child1 = createChildRequest(child1RequestId, parent, child1Points);
    Request child2 = createChildRequest(child2RequestId, parent, child2Points);

    // Remove the points from the parent
    parent.setPoints(new ArrayList<>());

    // Set back reference to the children
    parent.setChildRequestIds(Arrays.asList(child1RequestId, child2RequestId));

    // Store the requests
    requestRepository.save(parent);
    requestRepository.insert(child1);
    requestRepository.insert(child2);

    // Add variables to the execution so that they are available to the
    // recursive process invocation
    execution.setVariable("child1RequestId", child1.getRequestId());
    execution.setVariable("child2RequestId", child2.getRequestId());
    execution.setVariable("child1ContainsAlarms", child1.containsAlarms());
    execution.setVariable("child2ContainsAlarms", child2.containsAlarms());
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
