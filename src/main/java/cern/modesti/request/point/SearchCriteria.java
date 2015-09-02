package cern.modesti.request.point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {
  private String key;
  private String operation;
  private Object value;
}