/**
 *
 */
package cern.modesti.workflow;

import java.util.*;

import javax.persistence.Id;

/**
 * @author Justin Lewis Salmon
 *
 */
public class ApprovalResult {

  private Boolean approved;

  /** List of result items, one per point */
  private List<ApprovalResultItem> items = new ArrayList<>();

  public ApprovalResult() {
  }

  static class ApprovalResultItem {

    @Id
    private Long id;

    private boolean approved;

    private String message;

    public ApprovalResultItem() {
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
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

  /**
   * @return the approved
   */
  public Boolean isApproved() {
    return approved;
  }

  /**
   * @param approved the approved to set
   */
  public void setApproved(Boolean approved) {
    this.approved = approved;
  }

  /**
   * @return the items
   */
  public List<ApprovalResultItem> getItems() {
    return items;
  }

  /**
   * @param items the items to set
   */
  public void setItems(List<ApprovalResultItem> items) {
    this.items = items;
  }
}
