package cern.modesti.repository.jpa.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "draft_points")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DraftPoint {

  @Id
  @Column(name = "drp_lineno")
  private Long lineNumber;

  @Column(name = "drp_request_id")
  private Long requestId;

  // General
  @Column(name = "drp_pt_datatype")
  private String pointDataType;
  @Column(name = "drp_pt_desc")
  private String pointDescription;
  @Column(name = "drp_gmao_code")
  private String gmaoCode;
  @Column(name = "drp_other_equip_code")
  private String otherCode;
  @Column(name = "drp_pt_attribute")
  private String pointAttribute;
  @Column(name = "drp_resp_id")
  private Integer responsibleId;
  @Column(name = "drp_subsystem_id")
  private Integer subsystemId;
  @Column(name = "drp_moneq_id")
  private Integer monitoringEquipmentId;
  @Column(name = "drp_point_comp_info")
  private String pointComplementaryInfo;

  // Location
  @Column(name = "drp_bld_name")
  private String buildingName;
  @Column(name = "drp_bld_number")
  private String buildingNumber;
  @Column(name = "drp_bld_floor")
  private String buildingFloor;
  @Column(name = "drp_bld_room")
  private String buildingRoom;
  @Column(name = "drp_func_code")
  private String functionalityCode;
  @Column(name = "drp_zone_Secu")
  private String zone;

  // Alarms
  @Column(name = "drp_alarm_val")
  private Integer alarmValue;
  @Column(name = "drp_priority_code")
  private Integer priorityCode;
  @Column(name = "drp_category_name")
  private String alarmCategory;

  // Alarm Help
  @Column(name = "drp_alarm_causes")
  private String alarmCauses;
  @Column(name = "drp_alarm_conseq")
  private String alarmConsequences;
  @Column(name = "drp_work_hours_task")
  private String workHoursTask;
  @Column(name = "drp_outside_ours_ask")
  private String outsideHoursTask;

  // PLC (APIMMD)
  @Column(name = "drp_plc_blocktype")
  private Integer blockType;
  @Column(name = "drp_plc_wordid")
  private Integer wordId;
  @Column(name = "drp_plc_bitid")
  private Integer bitId;
  @Column(name = "drp_native_prefix")
  private String nativePrefix;
  @Column(name = "drp_slave_address")
  private Integer slaveAddress;
  @Column(name = "drp_connectid")
  private String connectId;


  @Column(name = "drp_exitcode")
  private Long exitCode;

  @Column(name = "drp_exittext")
  private String exitText;
}