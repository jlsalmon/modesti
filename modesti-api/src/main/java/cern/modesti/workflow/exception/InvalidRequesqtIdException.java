package cern.modesti.workflow.exception;

/**
 * Exception to be thrown when the request id is invalid.
 *  
 * @author Ivan Prieto Barreiro
 */
public class InvalidRequesqtIdException extends RuntimeException {
  private static final long serialVersionUID = -1795512813519299128L;

  /**
   * Exception constructor
   * @param msg Error message
   */
  public InvalidRequesqtIdException(String msg) {
    super(msg);
  }
 }
