/**
 *
 */
package cern.modesti.repository.jpa.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
//@Entity
public class ValidationResult implements Serializable {

  private static final long serialVersionUID = 1215131608580861894L;

  @Id
  private Long id;

  private Boolean valid;

  private List<String> errors;

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
   *
   */
  public ValidationResult() {
    this.id = 0L;
    this.valid = false;
    this.errors = new ArrayList<>(Arrays.asList("Field x is not valid", "Field y is out of range"));
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
