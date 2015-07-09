/**
 *
 */
package cern.modesti.repository.jpa.location.zone;

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

  public Zone() {
  }

  public Zone(String value) {
    this.value = value;
  }

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