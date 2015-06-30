package cern.modesti.repository.jpa.validation;

import cern.modesti.request.Request;

import javax.transaction.Transactional;

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
  //@Transactional
  ValidationResult validate(Request request);
}
