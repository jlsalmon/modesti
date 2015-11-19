package cern.modesti.request.point;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Error {
  private String property;
  private List<String> errors;
}
