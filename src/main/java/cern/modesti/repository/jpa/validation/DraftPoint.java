package cern.modesti.repository.jpa.validation;

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
  Long drp_request_id;
  @Id
  Long drp_lineno;
  String drp_pt_datatype;
  String drp_pt_desc;
  String drp_gmao_code;
  String drp_other_equip_code;

  Long drp_exitcode;
  String drp_exittext;

  public DraftPoint() {
  }

  public DraftPoint(Long drp_request_id, Long drp_lineno, String drp_pt_datatype, String drp_pt_desc, String drp_gmao_code, String drp_other_equip_code) {
    this.drp_request_id = drp_request_id;
    this.drp_lineno = drp_lineno;
    this.drp_pt_datatype = drp_pt_datatype;
    this.drp_pt_desc = drp_pt_desc;
    this.drp_gmao_code = drp_gmao_code;
    this.drp_other_equip_code = drp_other_equip_code;
  }

  public Long getExitCode() {
    return drp_exitcode;
  }

  public String getExitText() {
    return drp_exittext;
  }
}