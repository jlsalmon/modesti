package cern.modesti.schema.category;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Constraint {
  private String type;
  private List<String> members;
  private List<String> activeStates;
  private Condition condition;
}