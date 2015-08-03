package cern.modesti.workflow.history;

import lombok.Data;

import java.util.Date;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class HistoricEvent {
  private final Date startTime;
  private final Date endTime;
  private final Long duration;
  private final String name;
  private final String type;
  private final String description;
  private final String assignee;
}