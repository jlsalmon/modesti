package cern.modesti.repository.jpa.location;


import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Entity
public class BuildingName {

  @Id
  private String value;

  public BuildingName() {
  }

  public BuildingName(final String value) {
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
