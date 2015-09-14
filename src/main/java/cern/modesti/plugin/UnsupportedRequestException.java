package cern.modesti.plugin;

import cern.modesti.request.Request;

import static java.lang.String.format;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class UnsupportedRequestException extends RuntimeException {

  private static final long serialVersionUID = 7774908107032117480L;

  private final Request request;

  public UnsupportedRequestException(Request request) {
    super(format("no plugin found for domain %s", request.getDomain()));
    this.request = request;
  }

  /**
   * @return the request
   */
  public Request getRequest() {
    return request;
  }
}
