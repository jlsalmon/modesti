/**
 *
 */
package cern.modesti.model;

import javax.persistence.Id;

/**
 *
 * @author Justin Lewis Salmon
 *
 */
public class Datasource {

  @Id
  private String value;

  public Datasource() {
  }

  public Datasource(String value) {
    this.value = value;
  }

  /**
   * @return the name
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }
}
