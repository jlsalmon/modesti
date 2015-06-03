package cern.modesti.request.point;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class Approval {

  private Boolean approved;

  private String message;

  public Boolean isApproved() {
    return approved;
  }

  public void setApproved(Boolean approved) {
    this.approved = approved;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
