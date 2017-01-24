package cern.modesti.request.history;

import cern.modesti.request.Request;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.path.NodePath;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author Justin Lewis Salmon
 */
public class RequestDiffer {

  public static ChangeEvent diff(Request modified, Request original, String idProperty) {
    ChangeEvent event = new ChangeEvent(new DateTime(DateTimeZone.UTC));
    DiffNode root = ObjectDifferBuilder.startBuilding()
        .identity().ofCollectionItems(NodePath.with("points"))
        .via(new PointIdentityStrategy(idProperty))
        .and().build().compare(modified, original);

    root.visit(new ChangeVisitor(event, modified, original));
    return event;
  }
}
