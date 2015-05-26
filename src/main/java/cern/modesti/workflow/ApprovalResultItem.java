package cern.modesti.workflow;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class ApprovalResultItem {

  private boolean approved;

  private String message;

  public ApprovalResultItem() {
  }

  public boolean isApproved() {
    return approved;
  }

  public void setApproved(boolean approved) {
    this.approved = approved;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String error) {
    this.message = error;
  }
}