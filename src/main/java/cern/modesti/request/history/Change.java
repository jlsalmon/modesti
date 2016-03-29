package cern.modesti.request.history;

import de.danielbechler.diff.node.DiffNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Change {

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
  private Object original;

  /**
   * New value (empty for REMOVED)
   */
  private Object modified;
}
