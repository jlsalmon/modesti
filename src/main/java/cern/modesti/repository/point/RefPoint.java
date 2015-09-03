package cern.modesti.repository.point;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
@Entity
@Table(name = "VSML_220TAGINFO")
public class RefPoint {

  @Id
  @Column(name = "POINT_ID")
  private Long pointId;
  @Column(name = "CONTROL_FLAG")
  private String controlFlag;
  @Column(name = "ALARM_ID")
  private Long alarmId;
  @Column(name = "CREATE_REQUEST_ID")
  private Long createRequestId;
  @Column(name = "CHANGE_REQUEST_ID")
  private Long changeRequestId;
  @Column(name = "TIM_TAG_NAME")
  private String timTagName;
  @Column(name = "POINT_STATE")
  private Long pointState;
  @Column(name = "POINT_STATE_TEXT")
  private String pointStateText;
  @Column(name = "POINT_DATATYPE")
  private String pointDatatype;
  @Column(name = "SYSTEM_NAME")
  private String systemName;
  @Column(name = "SUBSYSTEM_NAME")
  private String subsystemName;
  @Column(name = "GMAO_CODE")
  private String gmaoCode;
  @Column(name = "OTHER_EQUIPMENT_CODE")
  private String otherEquipmentCode;
  @Column(name = "POINT_DESCRIPTION")
  private String pointDescription;
  @Column(name = "POINT_ATTRIBUTE")
  private String pointAttribute;
  @Column(name = "FUNCTIONALITY_CODE")
  private String functionalityCode;
  @Column(name = "GLOBAL_FUNCTIONALITY_CODE")
  private String globalFunctionalityCode;
  @Column(name = "BUILDING_NUMBER")
  private Long buildingNumber;
  @Column(name = "BUILDING_NAME")
  private String buildingName;
  @Column(name = "BUILDING_FLOOR")
  private String buildingFloor;
  @Column(name = "BUILDING_ROOM")
  private String buildingRoom;
  @Column(name = "POINT_COMPLEMENTARY_INFO")
  private String pointComplementaryInfo;
  @Column(name = "RESPONSIBLE_PERSON_ID")
  private Long responsiblePersonId;
  @Column(name = "RESPONSIBLE_LAST_NAME")
  private String responsibleLastName;
  @Column(name = "RESPONSIBLE_FIRST_NAME")
  private String responsibleFirstName;
  @Column(name = "FAULT_FAMILY")
  private String faultFamily;
  @Column(name = "FAULT_MEMBER")
  private String faultMember;
  @Column(name = "FAULT_CODE")
  private Long faultCode;
  @Column(name = "ALARM_VALUE")
  private String alarmValue;
  @Column(name = "PRIORITY_CODE")
  private Long priorityCode;
  @Column(name = "AUTOCALL_NUMBER")
  private Long autocallNumber;
  @Column(name = "LOW_LIMIT")
  private Long lowLimit;
  @Column(name = "HIGH_LIMIT")
  private Long highLimit;
  @Column(name = "VALUE_DEADBAND")
  private Long valueDeadband;
  @Column(name = "TIMBER_FLAG")
  private String timberFlag;
  @Column(name = "COMMAND_VALUE")
  private String commandValue;
  @Column(name = "MONITORING_EQUIPMENT_NAME")
  private String monitoringEquipmentName;
  @Column(name = "ADDRESS")
  private String address;
  @Column(name = "ADDRESS_HIERARCHY")
  private String addressHierarchy;
  @Column(name = "ALARM_FLAG")
  private String alarmFlag;
  @Column(name = "COMMAND_FLAG")
  private String commandFlag;
  @Column(name = "ANALOG_FLAG")
  private String analogFlag;
}
