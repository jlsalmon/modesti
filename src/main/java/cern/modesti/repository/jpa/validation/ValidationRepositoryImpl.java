package cern.modesti.repository.jpa.validation;

import cern.modesti.request.Request;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.util.List;

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

    writeRequest(request);

    StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("TIMPKREQCHECK.STP_CHECK_REQUEST");
    storedProcedure.registerStoredProcedureParameter("request_id", Long.class, ParameterMode.IN);
    storedProcedure.registerStoredProcedureParameter("exitcode", Integer.class, ParameterMode.OUT);
    storedProcedure.registerStoredProcedureParameter("exittext", String.class, ParameterMode.OUT);
    storedProcedure.setParameter("request_id", Long.valueOf(request.getRequestId()));
    storedProcedure.execute();

    Integer exitcode = (Integer) storedProcedure.getOutputParameterValue("exitcode");
    String exittext = (String) storedProcedure.getOutputParameterValue("exittext");

    ValidationResult result = new ValidationResult(exitcode, exittext);

    // TODO read the results

    return result;
  }

  /**
   * Write the points to the DRAFT_POINTS table
   *
   * @param request
   */
  private void writeRequest(Request request) {
    entityManager.createNativeQuery("INSERT INTO DRAFT_POINTS ('column1','column2') VALUES ('test1','test2')").executeUpdate();
  }

  private List<String> readResults() {
    return null;
  }
}
