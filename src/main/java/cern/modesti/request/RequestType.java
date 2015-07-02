package cern.modesti.request;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public enum RequestType {
  CREATE,
  MODIFY,
  DELETE;

  public enum Domain {
    TIM,
    CSAM,
    PVSS;
  }
}
