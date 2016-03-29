package cern.modesti.request.history;

import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;

import java.util.ArrayList;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
public class ChangeVisitor implements DiffNode.Visitor {

  private ChangeEvent event;
  private Request modified;
  private Request original;

  public ChangeVisitor(ChangeEvent event, Request modified, Request original) {
    this.event = event;
    this.modified = modified;
    this.original = original;

    if (event.getChanges() == null) {
      event.setChanges(new ArrayList<>());
    }
  }

  /**
   * Currently only changes to {@link Point#properties} are tracked. All others
   * are filtered out.
   *
   * If an entire object property changed, we make sure to record only a single
   * change as opposed to multiple changes for each field inside the object.
   *
   * @param node
   * @param visit
   */
  public void node(DiffNode node, Visit visit) {
    if ((isPrimitiveLeafProperty(node) || isObjectProperty(node)) && hasActuallyChanged(node)) {
      Change change = new Change(getPath(node), node.getState(), node.canonicalGet(original), node.canonicalGet(modified));
      event.getChanges().add(change);
    }
  }

  private boolean isPrimitiveLeafProperty(DiffNode node) {
    return (isProperty(node) && node.hasChanges() && !node.hasChildren());
  }

  private boolean isObjectProperty(DiffNode node) {
    return (isProperty(node) && node.hasChanges() && node.hasChildren());
  }

  private boolean isProperty(DiffNode node) {
    return node.getParentNode() != null
        && node.getParentNode().getPropertyName() != null
        && node.getParentNode().getPropertyName().equals("properties")
        && node.getParentNode().getParentNode().canonicalGet(modified) instanceof Point;
  }

  /**
   * If a property has changed from null to an empty string, we make sure to
   * not record a change.
   *
   * @param node
   * @return
   */
  private boolean hasActuallyChanged(DiffNode node) {
    Object object = node.canonicalGet(modified);
    return !(node.isAdded() && object instanceof String && ((String) object).isEmpty());
  }

  private String getPath(DiffNode node) {
    Point workingPoint = (Point) node.getParentNode().getParentNode().canonicalGet(modified);
    Long lineNo = workingPoint.getLineNo();
    String property = node.getPath().getLastElementSelector().toString();
    return format("/points[%d]/properties%s", lineNo, property);
  }
}
