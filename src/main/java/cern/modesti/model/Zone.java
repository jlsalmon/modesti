/**
 *
 */
package cern.modesti.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
@Entity
public class Zone {

  @Id
  private String value;

  /**
   * @return the value
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
