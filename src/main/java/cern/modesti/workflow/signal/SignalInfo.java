package cern.modesti.workflow.signal;

import lombok.Data;
import org.springframework.hateoas.core.Relation;

/**
 * Represents a workflow signal definition.
 *
 * @author Justin Lewis Salmon
 */
@Data
@Relation(collectionRelation = "signals")
public class SignalInfo {
  private final String name;
}
