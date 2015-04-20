/**
 *
 */
package cern.modesti.repository.request.datasource;

import javax.persistence.Id;

/**
 *
 * @author Justin Lewis Salmon
 *
 */
public class Datasource {

  @Id
  private String id;

  private String name;

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
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
