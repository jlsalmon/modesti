/**
 *
 */
package cern.modesti.repository.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

  @Id
  private String value;
  private String buildingNumber;
  private String floor;
  private String room;
}
