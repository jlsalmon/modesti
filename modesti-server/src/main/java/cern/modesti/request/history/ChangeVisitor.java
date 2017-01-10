package cern.modesti.request.history;

import cern.modesti.request.Request;
import cern.modesti.point.Point;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;

import java.util.ArrayList;

import static java.lang.String.format;

/**
 * A {@link de.danielbechler.diff.node.DiffNode.Visitor} implementation that
 * decides whether or not to record a particular change as a
 * {@link ChangeEvent}.
 *
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
   * Currently only changes to {@link Point#getProperties()} are tracked. All others
   * are filtered out.
   *
   * If an entire object property changed, we make sure to record only a single
   * change as opposed to multiple changes for each field inside the object.
   *
   * @param node the current node
   * @param visit the visit object
   */
  public void node(DiffNode node, Visit visit) {
    if ((isPrimitiveLeafProperty(node) || isObjectProperty(node)) && hasActuallyChanged(node)) {
      Change change = new Change(getPath(node), getLineNo(node), getProperty(node), node.getState().name(),
          node.canonicalGet(original), node.canonicalGet(modified));
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
   * @param node the current node
   * @return true if the property has actually changed, false otherwise
   */
  private boolean hasActuallyChanged(DiffNode node) {
    Object object = node.canonicalGet(modified);
    return !(node.isAdded() && object instanceof String && ((String) object).isEmpty());
  }

  private String getPath(DiffNode node) {
    return format("/points[%d]/properties{%s}", getLineNo(node), getProperty(node));
  }

  private String getProperty(DiffNode node) {
    String property = node.getPath().getLastElementSelector().toString();
    return property.substring(1, property.indexOf("}"));
  }

  private Long getLineNo(DiffNode node) {
    Point workingPoint = (Point) node.getParentNode().getParentNode().canonicalGet(modified);
    return workingPoint.getLineNo();
  }
}
