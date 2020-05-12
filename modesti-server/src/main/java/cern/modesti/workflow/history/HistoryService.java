package cern.modesti.workflow.history;

import cern.modesti.request.RequestRepository;
import cern.modesti.request.Request;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for retrieving historical workflow information.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class HistoryService {

  @Autowired
  RequestRepository requestRepository;

  @Autowired
  org.activiti.engine.HistoryService history;

  @Autowired
  TaskService taskService;

  @Autowired
  RepositoryService repositoryService;

  /**
   * Retrieve the workflow history for a particular request.
   *
   * @param requestId the id of the request
   * @return the list of historic events
   */
  public List<HistoricEvent> getHistoryForRequest(String requestId) {
    log.info("querying history for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new IllegalArgumentException("No request with id " + requestId + " was found");
    }

    List<HistoricEvent> historyEvents = new ArrayList<>();

    // Find the process instance for this request
    HistoricProcessInstance process = history.createHistoricProcessInstanceQuery().processInstanceBusinessKey(requestId).singleResult();

    // Find all the activities that happened so far for this process
    List<HistoricTaskInstance> tasks = history.createHistoricTaskInstanceQuery().processInstanceBusinessKey(requestId).orderByTaskCreateTime().asc().list();

    // Find all the activities that happened so far for this process
    List<HistoricActivityInstance> activities = history.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
        .orderByHistoricActivityInstanceStartTime().asc().list();
    
    for (HistoricActivityInstance activity : activities) {
      String description;
      String assignee;
      
      if ("userTask".equals(activity.getActivityType()) ) {
        description = getUserTaskDescription(tasks, activity.getTaskId());
        assignee = activity.getAssignee();
      } else if ("serviceTask".equals(activity.getActivityType())) {
        assignee = "admin";
        description = activity.getActivityName();
      } else {
        continue;
      }
      
      HistoricEvent event = new HistoricEvent(activity.getStartTime(), activity.getEndTime(), activity.getDurationInMillis(), activity.getActivityName(),
          activity.getActivityType(), description, assignee);
      historyEvents.add(event);
    }

    return historyEvents;
  }
  
  private String getUserTaskDescription(List<HistoricTaskInstance> tasks, String taskId) {
    for (HistoricTaskInstance task : tasks) {
      if (task.getId().equals(taskId)) {
        return task.getDescription();
      }
    }
    
    return "";
  }
}
