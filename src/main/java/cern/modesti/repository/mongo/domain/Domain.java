package cern.modesti.repository.mongo.domain;

import java.util.List;

import javax.persistence.Id;

/**
 *
 * @author Justin Lewis Salmon
 *
 */
public class Domain {

  @Id
  private String id;

  private String name;

  private List<String> datasources;

  private List<String> categories;

  public Domain() {
  }

  public Domain(String name) {
    this.name = name;
  }

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
  /**
   * @return the datasources
   */
  public List<String> getDatasources() {
    return datasources;
  }

  /**
   * @param datasources the datasources to set
   */
  public void setDatasources(List<String> datasources) {
    this.datasources = datasources;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }
}
