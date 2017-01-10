package cern.modesti.workflow.task;

import java.util.Set;

/**
 * @author Justin Lewis Salmon
 */
public interface TaskInfo {
  String getName();

  Set<String> getCandidateGroups();

  String getDescription();

  String getAssignee();
}
