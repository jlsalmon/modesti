package cern.modesti.repository.jpa.gmao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Entity
@Table(name = "mtf_objects")
public class GmaoCode {

  @Id
  @Column(name = "obj_code")
  private String value;

  public GmaoCode() {
  }

  public GmaoCode(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
