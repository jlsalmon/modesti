package cern.modesti.request.history;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import cern.modesti.point.Point;
import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.path.NodePath;

/**
 * @author Justin Lewis Salmon
 */
public class RequestDiffer {

  public static ChangeEvent diff(Request modified, Request original, String idProperty) {
    List<Point> originalPointsStillPresentCurrently = deleteRemovedPoints(original.getPoints(), modified.getPoints(), idProperty);
    original.setPoints(originalPointsStillPresentCurrently);

    ChangeEvent event = new ChangeEvent(new DateTime(DateTimeZone.UTC));
    Request modifiedClone = new RequestImpl();
    Request originalClone = new RequestImpl();
    ChangeVisitor visitor = new ChangeVisitor(event, modifiedClone, originalClone);

    Map<Object, Point> modifiedPointMap = getPointsMap(modified.getPoints(), idProperty);

    for (Point originalPoint : original.getPoints()) {
      Point modifiedPoint = modifiedPointMap.get(originalPoint.getValueByPropertyName(idProperty));
      if (originalPoint != null) {
        originalClone.setPoints(Arrays.asList(new Point[] { originalPoint }));
      }
      if (modifiedPoint != null) {
        modifiedClone.setPoints(Arrays.asList(new Point[] { modifiedPoint }));
      }

      ObjectDiffer differ = ObjectDifferBuilder.startBuilding().identity().ofCollectionItems(NodePath.with("points")).via(
          new PointIdentityStrategy(idProperty)).and().build();

      DiffNode root = differ.compare(modifiedClone, originalClone);
      if (root.hasChanges()) {
        root.visit(visitor);
      }
    }

    return event;
  }


  /**
   * Comparison does not work properly if some rows were removed, so we fix this by removing the same rows from original list,
   * before we start comparing states.
   *
   * @param originalPoints list of original points state
   * @param currentPoints  list of current points state
   * @param idProperty     property which serves as key to match points which we want to compare
   * @return list of original points without the ones missing in modified points
   */
  private static List<Point> deleteRemovedPoints(List<Point> originalPoints, List<Point> currentPoints, String idProperty) {
    if (isIdPropertyMissing(originalPoints, currentPoints, idProperty)) {
      return originalPoints;
    }
    Set<String> modifiedPointIds = currentPoints
        .stream()
        .map(point -> getIdPropertyFromPoint(idProperty, point))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
    List<Point> originalPointsCurrentlyPresent = originalPoints
        .stream()
        .filter(point -> modifiedPointIds.contains(getIdPropertyFromPoint(idProperty, point).orElse("non existent value")))
        .collect(Collectors.toList());
    AtomicLong atomicLong = new AtomicLong(1);
    originalPointsCurrentlyPresent.forEach(point -> point.setLineNo(atomicLong.getAndIncrement()));
    return originalPointsCurrentlyPresent;
  }

  private static boolean isIdPropertyMissing(List<Point> originalPoints, List<Point> modifiedPoints, String idProperty) {
    return isIdPropertyMissing(originalPoints, idProperty) || isIdPropertyMissing(modifiedPoints, idProperty);
  }

  private static boolean isIdPropertyMissing(List<Point> originalPoints, String idProperty) {
    return originalPoints.stream()
        .map(point -> getIdPropertyFromPoint(idProperty, point))
        .anyMatch(propertyValue -> !propertyValue.isPresent());
  }

  private static Optional<String> getIdPropertyFromPoint(String idProperty, Point point) {
    return Optional.ofNullable(point.getProperty(idProperty, Object.class))
        .map(Object::toString);
  }

  private static Map<Object, Point> getPointsMap(List<Point> points, String idProperty) {
    return points.stream()
         .filter(p -> Objects.nonNull(p.getValueByPropertyName(idProperty)))
         .collect(Collectors.toMap(p -> p.getValueByPropertyName(idProperty), p -> p));
  }
}
