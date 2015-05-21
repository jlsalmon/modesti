package cern.modesti.workflow;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class ConfigurationResult {

  @Id
  private Long id;

  private Boolean success;

  private List<String> errors;

  public ConfigurationResult() {
  }

  /**
   * Constructor to create a dummy validation result for testing.
   */
  public ConfigurationResult(boolean failed) {
    this.id = 0L;
    if (failed) {
      this.success = false;
      this.errors = new ArrayList<>(Arrays.asList("Duplicate key error", "Duplicate key error"));
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
   * @param valid the success to set
   */
  public void setSuccess(Boolean valid) {
    this.success = valid;
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
