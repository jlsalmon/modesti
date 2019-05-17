package cern.modesti.workflow.validation;

import cern.modesti.request.Request;

/** 
 * @author Ivan Prieto Barreiro
 */
public interface ValidationService {

  /**
   * Validate a request
   * 
   * @param request The request to validate
   * @return TRUE if and only if the request is valid
   */
  boolean validateRequest(Request request);
  
  /**
   * Pre validates a request. The method will be executed for requests
   * created from the REST service without using the MODESTI interface to 
   * verify that the minimum required information is provided (i.e. UPDATE/DELETE requests
   * contain at least the primary field)
   * 
   * @param request The request to pre-validate
   * @return TRUE if and only if the request is valid
   */
  boolean preValidateRequest(Request request);
}
