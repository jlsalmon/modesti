package cern.modesti.schema.field;


import lombok.*;

/**
 * A type of field that provides a static list of options. The list of options
 * can be either simple primitive values or {@link Option} instances that can
 * also provide a description of a primitive value.
 *
 * @author Justin Lewis Salmon
 */
public class OptionsField extends Field {
  @Getter
  @Setter
  private Object options;
}
