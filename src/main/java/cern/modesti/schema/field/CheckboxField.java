package cern.modesti.schema.field;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class CheckboxField extends Field {

  @Getter
  @Setter
  private Boolean selected;
}
