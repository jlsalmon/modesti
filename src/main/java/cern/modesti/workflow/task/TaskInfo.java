package cern.modesti.workflow.task;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

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
}
