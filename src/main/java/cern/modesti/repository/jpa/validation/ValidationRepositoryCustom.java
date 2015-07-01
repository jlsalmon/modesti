package cern.modesti.repository.jpa.validation;

import cern.modesti.request.Request;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


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
  boolean validate(Request request);
}
