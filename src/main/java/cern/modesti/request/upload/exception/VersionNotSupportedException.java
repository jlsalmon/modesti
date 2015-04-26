/**
 *
 */
package cern.modesti.request.upload.exception;

/**
 * @author Justin Lewis Salmon
 *
 */
public class VersionNotSupportedException extends RuntimeException {

  private static final long serialVersionUID = 3878619384566019345L;

  /**
   *
   */
  public VersionNotSupportedException(String message) {
    super(message);
  }
}
