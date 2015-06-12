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
public class Location {

  @Id
  private String buildingNumber;

  private String floor;

  private String room;

  /**
   * Combined string of the location in the format NUMBER/FLOOR-ROOM (e.g. 104/R-A01)
   */
  private String value;

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
