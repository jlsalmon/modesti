package cern.modesti.workflow;

import cern.modesti.repository.jpa.validation.ValidationRepository;
import cern.modesti.repository.jpa.validation.ValidationResult;
import cern.modesti.request.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
public class ValidationService {

  @Autowired
  ValidationRepository repository;

  /**
   *
   * @param request
   * @return
   */
  public ValidationResult validateRequest(Request request) {
    // Call the stored procedure and read the results
    return repository.validate(request);
  }
}
