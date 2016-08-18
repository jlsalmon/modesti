package cern.modesti.request.upload.exception;

/**
 * To be thrown when the parsing of an uploaded request fails.
 *
 * @author Justin Lewis Salmon
 */
public class RequestParseException extends RuntimeException {

  private static final long serialVersionUID = -2584910082114169155L;

  public RequestParseException(String message) {
    super(message);
  }

  public RequestParseException(Exception e) {
    super(e);
  }
}
