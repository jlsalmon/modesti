/**
 *
 */
package cern.modesti.repository.jpa.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
public class ValidationResult {

  @Id
  private Long id;

  private Boolean valid;

  private List<String> errors;

  public ValidationResult() {
  }

  /**
   * Constructor to create a dummy validation result for testing.
   */
  public ValidationResult(boolean failed) {
    this.id = 0L;
    if (failed) {
      this.valid = false;
      this.errors = new ArrayList<>(Arrays.asList("Field x is not valid", "Field y is out of range"));
    } else {
      this.valid = true;
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
   * @return the valid
   */
  public Boolean isValid() {
    return valid;
  }

  /**
   * @param valid the valid to set
   */
  public void setValid(Boolean valid) {
    this.valid = valid;
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
