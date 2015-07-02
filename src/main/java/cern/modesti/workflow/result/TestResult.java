/**
 *
 */
package cern.modesti.workflow.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
public class TestResult {

  @Id
  private Long id;

  private Boolean passed;

  private List<String> errors;

  public TestResult() {
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
   * @return the passed
   */
  public Boolean getPassed() {
    return passed;
  }

  /**
   * @param passed the passed to set
   */
  public void setPassed(Boolean passed) {
    this.passed = passed;
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
