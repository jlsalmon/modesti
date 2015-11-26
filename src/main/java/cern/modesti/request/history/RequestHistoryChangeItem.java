package cern.modesti.request.history;

import de.danielbechler.diff.node.DiffNode;
import lombok.Data;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class RequestHistoryChangeItem {

  /**
   * Path to changed property (PropertyPath)
   */
  private String path;

  /**
   * Change state (ADDED, CHANGED, REMOVED etc.)
   */
  private DiffNode.State state;

  /**
   * Original value (empty for ADDED)
   */
  private Object base;

  /**
   * New value (empty for REMOVED)
   */
  private Object modified;
}
