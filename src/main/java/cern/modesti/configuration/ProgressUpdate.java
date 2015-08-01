package cern.modesti.configuration;

import lombok.Data;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class ProgressUpdate {
  private Integer progress;
  private String action;
}
