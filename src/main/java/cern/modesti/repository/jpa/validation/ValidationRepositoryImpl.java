package cern.modesti.repository.jpa.validation;

import cern.modesti.request.Request;

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

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public ValidationResult validate(Request request) {

    StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("TIMPKREQCHECK.STP_CHECK_REQUEST");
    storedProcedure.registerStoredProcedureParameter("request_id", Long.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("exitcode", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("exittext", String.class, ParameterMode.OUT);
    storedProcedure.setParameter("request_id", Long.valueOf(request.getRequestId()));
    storedProcedure.execute();

    Integer exitcode = (Integer) storedProcedure.getOutputParameterValue("exitcode");
    String exittext = (String) storedProcedure.getOutputParameterValue("exittext");

    return new ValidationResult(exitcode, exittext);
  }
}
