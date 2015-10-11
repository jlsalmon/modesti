package cern.modesti.repository.location.functionality;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Justin Lewis Salmon
 */
@Entity
@Table(name = "functionalities")
public class Functionality{

  @Id
  @Column(name = "func_code")
  private String value;

  @Column(name = "func_gen")
  private String generalFunctionality;

  public Functionality() {
  }

  public Functionality(final String value) {
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

  public String getGeneralFunctionality() {
    return generalFunctionality;
  }

  public void setGeneralFunctionality(String generalFunctionality) {
    this.generalFunctionality = generalFunctionality;
  }
}
