package cern.modesti.workflow.validation;

import cern.modesti.request.Request;

/** 
 * @author Ivan Prieto Barreiro
 */
public interface ValidationService {

  /**
   * Validate a request
   * 
   * @param request
   * @return
   */
  boolean validateRequest(Request request);
}
