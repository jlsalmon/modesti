package cern.modesti.elastic;

import javax.persistence.Id;

//@Document(indexName = "buildings")
public class Building {

  @Id
  private String id;

  private String name;

  private String number;

  public Building() {
  }

  public Building(final String id, final String name, final String number) {
    this.id = id;
    this.name = name;
    this.number = number;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }
}
