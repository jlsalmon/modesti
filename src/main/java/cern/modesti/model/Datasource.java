/**
 *
 */
package cern.modesti.model;

import javax.persistence.Id;

/**
 *
 * @author Justin Lewis Salmon
 *
 */
public class Datasource {

  @Id
  private String name;
  
  public Datasource() {
  }
  
  public Datasource(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
}
