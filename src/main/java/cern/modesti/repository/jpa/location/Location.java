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

  @Id
  private String id;
  
  private String location;
  
  /**
   * @return the location
   */
  public String getLocation() {
    return location;
  }
  
  /**
   * @param location the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }
}
