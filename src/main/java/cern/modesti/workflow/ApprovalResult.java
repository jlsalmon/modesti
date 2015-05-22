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

  @Id
  private Long id;

  private Boolean approved;

  /** Map of point ids to result item */
  private Map<Long, ApprovalResultItem> items = new HashMap<>();

  public ApprovalResult() {
  }

  class ApprovalResultItem {

    private boolean approved;

    private String message;

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
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
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
  public Map<Long, ApprovalResultItem> getItems() {
    return items;
  }

  /**
   * @param items the items to set
   */
  public void setItems(Map<Long, ApprovalResultItem> items) {
    this.items = items;
  }
}
