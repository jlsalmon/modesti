package cern.modesti.request;

/**
 * Search for a valid domain if the request does not provide it.
 * 
 * @author Ivan Prieto Barreiro
 */
public interface RequestDomainSearch {

  /**
   * Find a domain for the request
   * @param request The request for which a domain is required
   * @return The domain for the request
   */
  String find(Request request);
}
