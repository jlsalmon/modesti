package cern.modesti.repository.jpa.validation;

import cern.modesti.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class ValidationRepositoryImpl implements ValidationRepositoryCustom {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationRepositoryImpl.class);

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public boolean validate(Request request) {
    LOG.debug("validating via stored procedure");

    // Make sure that the draft points have been properly flushed to the database
    entityManager.flush();

    // Create and call the stored procedure
    StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("TIMPKREQCHECK.STP_CHECK_REQUEST");
    storedProcedure.registerStoredProcedureParameter(0, Integer.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter(1, Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter(2, String.class, ParameterMode.OUT);
    storedProcedure.setParameter(0, Integer.valueOf(request.getRequestId()));
    storedProcedure.execute();

    // Get the output parameters
    Integer exitcode = (Integer) storedProcedure.getOutputParameterValue(1);
    String exittext = (String) storedProcedure.getOutputParameterValue(2);

    // Clear the persistence context so that we get the exit codes and messages when
    // we read back the processed draft points
    entityManager.clear();

    LOG.debug(String.format("validation result: (%d) %s", exitcode, exittext));
    return exitcode == 0;
  }
}
