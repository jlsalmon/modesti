package cern.modesti.workflow.signal;

import lombok.Data;
import org.springframework.hateoas.core.Relation;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@Relation(collectionRelation = "signals")
public class SignalInfo {
  private final String name;
}