package cern.modesti.workflow.history;

import cern.modesti.request.Request;
import lombok.Data;

import java.util.Date;

/**
 * This class represents a single event in the history of a workflow for a
 * {@link Request}.
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
