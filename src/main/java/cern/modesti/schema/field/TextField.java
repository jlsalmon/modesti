package cern.modesti.schema.field;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Justin Lewis Salmon
 */
public class TextField extends Field {

  /**
   * Defines whether this field should be transformed to uppercase.
   */
  @Getter
  @Setter
  private Boolean uppercase;
}
