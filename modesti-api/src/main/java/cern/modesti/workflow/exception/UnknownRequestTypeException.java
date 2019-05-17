package cern.modesti.workflow.exception;

/**
 * Exception to be thrown when the request type is unknown.
 *  
 * @author Ivan Prieto Barreiro
 */
public class UnknownRequestTypeException extends RuntimeException {
  private static final long serialVersionUID = 4428976271630477632L;

  /**
   * Exception constructor
   * @param msg Error message
   */
  public UnknownRequestTypeException(String msg) {
    super(msg);
  }
}
