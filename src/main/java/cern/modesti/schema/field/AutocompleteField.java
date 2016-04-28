package cern.modesti.schema.field;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A type of field that can retrieve a list of values from a REST endpoint URL
 * based on a query string (and optionally other parameters, such as the value
 * of another field).
 *
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
