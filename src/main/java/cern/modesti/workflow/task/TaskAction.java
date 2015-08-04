package cern.modesti.workflow.task;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class TaskAction {
  private Action action;
  private String assignee;

  public enum Action {
    CLAIM, COMPLETE, DELEGATE
  }
}
