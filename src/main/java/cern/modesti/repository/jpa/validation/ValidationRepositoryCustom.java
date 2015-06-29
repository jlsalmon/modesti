package cern.modesti.repository.jpa.validation;

import cern.modesti.request.Request;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface ValidationRepositoryCustom {

  /**
   *
   * @param request
   * @return
   */
  ValidationResult validate(Request request);
}
