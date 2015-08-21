package cern.modesti.repository.equipment;

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
@Table(name = "monequipment")
public class MonitoringEquipment {

  @Id
  @Column(name = "moe_moneq_id")
  private Integer id;

  @Column(name = "moe_impname")
  private String name;

  @Column(name = "moe_moneq_name")
  private String value;

  public MonitoringEquipment() {
  }

  public MonitoringEquipment(Integer id, String name, String value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
