package cern.modesti.schema.category;

import lombok.Data;

/**
 * This class represents a MODESTI schema conditional definition.
 *
 * @author Justin Lewis Salmon
 */
@Data
public class Condition {
  private String operation;
  private String field;
  private Object value;
}
