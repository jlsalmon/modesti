package cern.modesti.schema.category;

import lombok.Data;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class Condition {
  private String operation;
  private String field;
  private String value;
}
