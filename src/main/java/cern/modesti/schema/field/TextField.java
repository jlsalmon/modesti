package cern.modesti.schema.field;

import lombok.Getter;
import lombok.Setter;

/**
 * A type of field that holds a simple textual value.
 *
 * @author Justin Lewis Salmon
 */
public class TextField extends Field {

  @Getter
  @Setter
  private String url;

  @Getter
  @Setter
  private Boolean strict;

  /**
   * Defines whether this field should be transformed to uppercase.
   */
  @Getter
  @Setter
  private Boolean uppercase;
}
