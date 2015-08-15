/**
 *
 */
package cern.modesti.repository.jpa.location.zone;

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
@NoArgsConstructor
@AllArgsConstructor
public class SafetyZone {
  @Id
  private String value;
}
