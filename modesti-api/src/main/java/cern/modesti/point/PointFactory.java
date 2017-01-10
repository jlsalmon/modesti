package cern.modesti.point;

/**
 * @author Justin Lewis Salmon
 */
public interface PointFactory {

  static Point createPoint() {
    Point point;

    try {
      point = (Point) Class.forName("cern.modesti.point.PointImpl").newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException("Error creating Point instance", e);
    }

    return point;
  }
}
