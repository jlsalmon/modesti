/**
 *
 */
package cern.modesti.repository.jpa.validation;

import oracle.net.aso.e;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.*;

/**
 * @author Justin Lewis Salmon
 */
@Entity
@NamedStoredProcedureQuery(name = "ValidationResult.STP_CHECK_REQUEST", procedureName = "TIMPKREQCHECK.STP_CHECK_REQUEST", parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "request_id", type = Long.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "exitcode", type = Integer.class)
})
public class ValidationResult {

  @Id
  @GeneratedValue
  private Integer exitcode;

  @GeneratedValue
  private String exittext;

  public ValidationResult() {
  }

  public ValidationResult(Integer exitcode, String exittext) {
    this.exitcode = exitcode;
    this.exittext = exittext;
  }

  public Integer getExitcode() {
    return exitcode;
  }

  public String getExittext() {
    return exittext;
  }
}
