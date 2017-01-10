package cern.modesti.schema.field;

import lombok.Getter;
import lombok.Setter;

/**
 * A type of field that is rendered as a checkbox and has a boolean state.
 *
 * @author Justin Lewis Salmon
 */
public class CheckboxField extends Field {

  @Getter
  @Setter
  private Boolean selected;
}
