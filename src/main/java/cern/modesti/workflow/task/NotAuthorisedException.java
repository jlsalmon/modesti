package cern.modesti.workflow.task;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class NotAuthorisedException extends RuntimeException {

  private static final long serialVersionUID = 3878619384566019345L;

  /**
   *
   */
  public NotAuthorisedException(String message) {
    super(message);
  }
}
