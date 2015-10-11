package cern.modesti.schema.field;


import lombok.*;

/**
 * @author Justin Lewis Salmon
 */
public class OptionsField extends Field {
  @Getter
  @Setter
  private Object options;
}
