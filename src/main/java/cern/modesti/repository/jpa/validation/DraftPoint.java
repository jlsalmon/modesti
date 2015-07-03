package cern.modesti.repository.jpa.validation;

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
public class DraftPoint {

  @Id
  @Column(name = "drp_lineno")
  Long lineNumber;

  @Column(name = "drp_request_id")
  Long requestId;

  @Column(name = "drp_pt_datatype")
  String datatype;

  @Column(name = "drp_pt_desc")
  String pointDescription;

  @Column(name = "drp_gmao_code")
  String gmaoCode;

  @Column(name = "drp_other_equip_code")
  String otherCode;

  @Column(name = "drp_func_code")
  String functionalityCode;

  @Column(name = "drp_bld_name")
  String buildingName;

  @Column(name = "drp_bld_number")
  String buildingNumber;

  @Column(name = "drp_bld_floor")
  String buildingFloor;

  @Column(name = "drp_bld_room")
  String buildingRoom;

  @Column(name = "drp_pt_attribute")
  String pointAttribute;

  @Column(name = "drp_resp_id")
  Integer responsibleId;

  @Column(name = "drp_subsystem_id")
  Integer subsystemId;

  @Column(name = "drp_moneq_id")
  Integer monitoringEquipmentId;

  @Column(name = "drp_exitcode")
  Long exitCode;

  @Column(name = "drp_exittext")
  String exitText;

  public DraftPoint() {
  }

  public DraftPoint(Long requestId, Long lineNumber, String datatype, String pointDescription, String gmaoCode, String otherCode, String functionalityCode,
                    String buildingName, String buildingNumber, String buildingFloor, String buildingRoom, String pointAttribute, Integer responsibleId,
                    Integer subsystemId, Integer monitoringEquipmentId) {
    this.requestId = requestId;
    this.lineNumber = lineNumber;
    this.datatype = datatype;
    this.pointDescription = pointDescription;
    this.gmaoCode = gmaoCode;
    this.otherCode = otherCode;
    this.functionalityCode = functionalityCode;
    this.buildingName = buildingName;
    this.buildingNumber = buildingNumber;
    this.buildingFloor = buildingFloor;
    this.buildingRoom = buildingRoom;
    this.pointAttribute = pointAttribute;
    this.responsibleId = responsibleId;
    this.subsystemId = subsystemId;
    this.monitoringEquipmentId = monitoringEquipmentId;
  }

  public Long getLineNumber() {
    return lineNumber;
  }

  public Long getRequestId() {
    return requestId;
  }

  public String getDatatype() {
    return datatype;
  }

  public String getDescription() {
    return pointDescription;
  }

  public String getGmaoCode() {
    return gmaoCode;
  }

  public String getOtherCode() {
    return otherCode;
  }

  public String getFunctionalityCode() {
    return functionalityCode;
  }

  public String getPointAttribute() {
    return pointAttribute;
  }

  public Long getExitCode() {
    return exitCode;
  }

  public String getExitText() {
    return exitText;
  }

  @Override
  public String toString() {
    return String.format("DraftPoint[lineNumber=%d, requestId='%d', datatype='%s', pointDescription='%s', exitCode='%d', exitText='%s']", lineNumber,
        requestId, datatype, pointDescription, exitCode, exitText);
  }
}