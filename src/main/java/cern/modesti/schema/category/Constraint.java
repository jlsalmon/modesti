package cern.modesti.schema.category;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class represents a MODESTI schema constraint definition.
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Constraint {
  private String type;
  private List<String> members;
  private Condition condition;
}
