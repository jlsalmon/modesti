package cern.modesti.request;

/**
 * Created by jsalmon on 4/28/15.
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
