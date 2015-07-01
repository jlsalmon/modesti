package cern.modesti.request.point;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class Error {
  private String property;

  private List<String> errors;

  public Error() {
  }

  public Error(String property, List<String> errors) {
    this.property = property;
    this.errors = errors;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }
}
