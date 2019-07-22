package cern.modesti.request;

import static java.lang.String.format;

/**
 * Exception to be thrown when the request is not found in the repository.
 * 
 * @author Ivan Prieto Barreiro
 */
public class RequestNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 6040621589684501632L;

  /**
   * Class constructor
   * @param id The request identifier
   */
  public RequestNotFoundException(String id) {
    super(format("Request #%s was not found", id));
  }
}
