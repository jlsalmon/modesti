package cern.modesti.workflow.task;

import lombok.Data;
import org.activiti.engine.task.DelegationState;
import org.springframework.hateoas.core.Relation;

import java.util.Set;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@Relation(collectionRelation = "tasks")
public class TaskInfo {
  private final String name;
  private final String description;
  private final String owner;
  private final String assignee;
  private final DelegationState delegationState;
  private final Set<String> candidateGroups;
}
