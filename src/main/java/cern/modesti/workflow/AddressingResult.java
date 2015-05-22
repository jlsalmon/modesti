/**
 *
 */
package cern.modesti.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
public class AddressingResult {

  @Id
  private Long id;

  private Boolean addressed;

  private List<String> errors;

  public AddressingResult() {
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the ${workflowService.onApprovalCompleted(requestId, execution)}
   */
  public Boolean isAddressed() {
    return addressed;
  }

  /**
   * @param addressed the addressed to set
   */
  public void setAddressed(Boolean addressed) {
    this.addressed = addressed;
  }

  /**
   * @return the errors
   */
  public List<String> getErrors() {
    return errors;
  }

  /**
   * @param errors the errors to set
   */
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }
}
