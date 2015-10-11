package cern.modesti.repository.subsystem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * @author Justin Lewis Salmon
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubSystem {
  @Id
  private Long id;
  private String value;
  private String system;
  private String systemCode;
  private String subsystem;
  private String subsystemCode;
}
