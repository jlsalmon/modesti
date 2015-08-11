package cern.modesti.request.point.state;

import lombok.Data;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class Approval {
  private Boolean approved;
  private String message;
}
