package cern.modesti.workflow.history;

import java.util.Date;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
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