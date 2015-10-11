package cern.modesti.repository.alarm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Justin Lewis Salmon
 */
@Entity
@Table(name="valm_leafcats")
public class AlarmCategory {

  @Id
  @Column(name = "cat_name")
  private String value;

  public AlarmCategory() {
  }

  public AlarmCategory(final String value) {
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
