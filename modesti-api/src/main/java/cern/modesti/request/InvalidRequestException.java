package cern.modesti.request;

/**
 * Exception thrown when a request is invalid
 * 
 * @author Ivan Prieto Barreiro
 */
public class InvalidRequestException extends RuntimeException {

  private static final long serialVersionUID = 6189290152135965130L;

  /**
   * Class constructor
   * @param msg Error message
   */
  public InvalidRequestException (String msg) {
    super(msg);
  }
}
