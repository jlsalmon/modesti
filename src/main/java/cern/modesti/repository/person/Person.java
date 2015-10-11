package cern.modesti.repository.person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * @author Justin Lewis Salmon
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person  {
  @Id
  private Long id;
  private String name;
  private String username;
}
