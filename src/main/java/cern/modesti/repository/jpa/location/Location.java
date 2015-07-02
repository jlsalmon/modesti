/**
 *
 */
package cern.modesti.repository.jpa.location;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
@Entity
public class Location {

  /**
   * Combined string of the location in the format NUMBER/FLOOR-ROOM (e.g. 104/R-A01)
   */
  @Id
  private String value;

  private String buildingNumber;

  private String floor;

  private String room;

  /**
   * @return the buildingNumber
   */
  public String getBuildingNumber() {
    return buildingNumber;
  }

  /**
   * @param buildingNumber the buildingNumber to set
   */
  public void setBuildingNumber(String buildingNumber) {
    this.buildingNumber = buildingNumber;
  }

  public String getFloor() {
    return floor;
  }

  public void setFloor(String floor) {
    this.floor = floor;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
