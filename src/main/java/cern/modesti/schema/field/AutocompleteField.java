package cern.modesti.schema.field;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public class AutocompleteField extends Field {

  @Getter
  @Setter
  private String url;

  @Getter
  @Setter
  private List<String> params;
}
