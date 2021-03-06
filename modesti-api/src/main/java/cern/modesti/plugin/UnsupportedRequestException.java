package cern.modesti.plugin;

import cern.modesti.request.Request;

import static java.lang.String.format;

/**
 * To be thrown when an attempt is made to act upon a {@link Request} for which
 * no related plugin could be found.
 *
 * @author Justin Lewis Salmon
 */
public class UnsupportedRequestException extends RuntimeException {

  private static final long serialVersionUID = 7774908107032117480L;

  private final Request request;

  public UnsupportedRequestException(Request request) {
    super(String.format("no plugin found for domain %s", request.getDomain()));
    this.request = request;
  }

  public UnsupportedRequestException(String message) {
    super(message);
    this.request = null;
  }

  /**
   * @return the request
   */
  public Request getRequest() {
    return request;
  }
}
