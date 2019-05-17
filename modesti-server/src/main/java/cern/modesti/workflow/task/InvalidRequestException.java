package cern.modesti.workflow.task;

/**
 * Thrown when the request service receives an invalid request (e.g. delete/update request with empty points) 
 * @author Ivan Prieto Barreiro
 */
public class InvalidRequestException extends RuntimeException {
  private static final long serialVersionUID = -3712938219674337814L;

  /**
   * Class constructor
   * @param msg Error message
   */
  public InvalidRequestException(String msg) {
    super(msg);
  }
}
