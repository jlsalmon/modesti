package cern.modesti.request.upload.exception;

/**
 * To be thrown when an attempt is made to upload a request of an unsupported
 * version.
 *
 * @author Justin Lewis Salmon
 */
public class VersionNotSupportedException extends RuntimeException {

  private static final long serialVersionUID = 3878619384566019345L;

  public VersionNotSupportedException(String message) {
    super(message);
  }
}
