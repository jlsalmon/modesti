package cern.modesti.request.point;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Error {
  private String property;
  private List<String> errors;
}
