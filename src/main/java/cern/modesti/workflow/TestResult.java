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
public class TestResult {

  @Id
  private Long id;

  private Boolean success;

  private List<String> errors;

  public TestResult() {
  }

  /**
   * Constructor to create a dummy test result for testing.
   */
  public TestResult(boolean failed) {
    this.id = 0L;
    if (failed) {
      this.success = false;
      this.errors = new ArrayList<>(Arrays.asList("Point 1 failed", "Point 2 failed"));
    } else {
      this.success = true;
      this.errors = new ArrayList<>();
    }
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
   * @return the success
   */
  public Boolean getSuccess() {
    return success;
  }

  /**
   * @param success the success to set
   */
  public void setSuccess(Boolean success) {
    this.success = success;
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
