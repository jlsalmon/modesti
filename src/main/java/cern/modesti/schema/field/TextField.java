package cern.modesti.schema.field;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Justin Lewis Salmon
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TextField extends Field {

  /**
   * Defines whether this field should be transformed to uppercase.
   */
  private Boolean uppercase;
}
