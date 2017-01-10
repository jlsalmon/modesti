package cern.modesti.request.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single change (add, modify, remove) of a single property.
 *
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
   * The line number
   */
  private Long lineNo;

  /**
   * The name of the property (map key)
   */
  private String property;

  /**
   * Change state (ADDED, CHANGED, REMOVED etc.)
   */
  private String state;

  /**
   * Original value (empty for ADDED)
   */
  private Object original;

  /**
   * New value (empty for REMOVED)
   */
  private Object modified;
}
