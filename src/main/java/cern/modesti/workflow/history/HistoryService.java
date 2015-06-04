package cern.modesti.workflow.history;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
public class HistoryService {
  private static final Logger LOG = LoggerFactory.getLogger(HistoryService.class);

  @Autowired
  RequestRepository requestRepository;

  @Autowired
  org.activiti.engine.HistoryService historyService;

  @Autowired
  TaskService taskService;

  @Autowired
  RepositoryService repositoryService;


  public List<HistoricEvent> getHistoryForRequest(String requestId) {
    LOG.info("querying history for request id " + requestId + "...");

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new IllegalArgumentException("No request with id " + requestId + " was found");
    }

    List<HistoricEvent> history = new ArrayList<>();

    // Find the process instance for this request
    HistoricProcessInstance process = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(requestId).singleResult();

    // Find all the activities that happened so far for this process
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(process.getId()).orderByTaskCreateTime().asc().list();

    // Find all the activities that happened so far for this process
    List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
        .orderByHistoricActivityInstanceStartTime().asc().list();

    for (HistoricActivityInstance activity : activities) {
      // Not interested in gateway activities
      if (!activity.getActivityType().equals("exclusiveGateway") && !activity.getActivityType().equals("parallelGateway")) {

        String description = "";
        for (HistoricTaskInstance task : tasks) {
          if (task.getExecutionId().equals(activity.getExecutionId())) {
            description = task.getDescription();
          }
        }

        HistoricEvent event = new HistoricEvent(activity.getStartTime(), activity.getEndTime(), activity.getDurationInMillis(), activity.getActivityName(),
            activity.getActivityType(), description, activity.getAssignee());
        history.add(event);
      }
    }

    return history;
  }


  public class HistoricEvent {
    private Date startTime;
    private Date endTime;
    private Long duration;
    private String name;
    private String type;
    private String description;
    private String assignee;

    public HistoricEvent(Date startTime, Date endTime, Long duration, String name, String type, String description, String assignee) {
      this.startTime = startTime;
      this.endTime = endTime;
      this.duration = duration;
      this.name = name;
      this.type = type;
      this.description = description;
      this.assignee = assignee;
    }

    public Date getStartTime() {
      return startTime;
    }

    public void setStartTime(Date startTime) {
      this.startTime = startTime;
    }

    public Date getEndTime() {
      return endTime;
    }

    public void setEndTime(Date endTime) {
      this.endTime = endTime;
    }

    public Long getDuration() {
      return duration;
    }

    public void setDuration(Long duration) {
      this.duration = duration;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getAssignee() {
      return assignee;
    }

    public void setAssignee(String assignee) {
      this.assignee = assignee;
    }
  }
}
