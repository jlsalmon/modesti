package cern.modesti.request.history;

import cern.modesti.request.Request;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;

import java.util.ArrayList;

/**
 * @author Justin Lewis Salmon
 */
public class RequestHistoryChangeVisitor implements DiffNode.Visitor {

  private RequestHistoryChange change;
  private Request working;
  private Request base;

  public RequestHistoryChangeVisitor(RequestHistoryChange change, Request working, Request base) {
    this.change = change;
    this.working = working;
    this.base = base;
  }

  public void node(DiffNode node, Visit visit) {
    if (node.isRootNode() && !node.hasChanges() || node.hasChanges() && !node.hasChildren()) {
      RequestHistoryChangeItem diffItem = new RequestHistoryChangeItem();

      diffItem.setPath(node.getPath().toString());
      diffItem.setState(node.getState());

      if (node.getState() != DiffNode.State.UNTOUCHED) {
        diffItem.setBase(node.canonicalGet(base));
        diffItem.setModified(node.canonicalGet(working));
      }

      if (change.getItems() == null) {
        change.setItems(new ArrayList<>());
      }

      change.getItems().add(diffItem);
    }
  }
}
