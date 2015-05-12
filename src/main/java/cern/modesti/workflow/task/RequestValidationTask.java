package cern.modesti.workflow.task;

import cern.modesti.repository.jpa.validation.ValidationResult;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Random;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Transactional
public class RequestValidationTask {
  private static final Logger LOG = LoggerFactory.getLogger(RequestValidationTask.class);

  @Autowired
  private RequestRepository requestRepository;

  /**
   *
   * @param requestId
   * @param execution
   */
  public void validateRequest(String requestId, DelegateExecution execution) {
    LOG.info("validating request " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }


    /**
     * Perform actual request validation here
     */


    // Randomly fail the validation
    boolean failed = new Random(System.currentTimeMillis()).nextBoolean();

    if (failed) {
      execution.setVariable("validationResult", new ValidationResult());
    }

    // Set the variable for the next stage to evaluate
    execution.setVariable("containsErrors", failed);
  }
}
