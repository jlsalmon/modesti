package cern.modesti.workflow.task;

import cern.modesti.security.ldap.Role;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.activiti.engine.task.DelegationState;
import org.springframework.hateoas.core.Relation;
import org.springframework.security.core.GrantedAuthority;

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
