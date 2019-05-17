package cern.modesti.workflow.task;

/**
 * To be thrown when an attempt is made to perform an action on a workflow task
 * for which the user is not authorised.
 *
 * @author Justin Lewis Salmon
 */
public class NotAuthorisedException extends RuntimeException {

  private static final long serialVersionUID = 3878619384566019345L;

  /**
   * Class constructor
   * @param message Error message
   */
  public NotAuthorisedException(String message) {
    super(message);
  }
}
