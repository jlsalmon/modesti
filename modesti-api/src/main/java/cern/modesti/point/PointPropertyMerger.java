package cern.modesti.point;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class to merge point properties
 * 
 * @author Ivan Prieto Barreiro
 */
public final class PointPropertyMerger {
  
  private PointPropertyMerger() {
    // Private, empty constructor
  }

  /**
   * Merges the properties of each point in the srcPoints to each corresponding point in the dstPoints ONLY IF the 
   * dst point does not contain a value for the property.
   * 
   * @param srcPoints The list of points where to copy the property values
   * @param dstPoints The list of points where to put the property values
   * @param getPointIdentity Function to get the unique identifier of the point
   */
  public static <T> void mergePointProperties(List<Point> srcPoints, List<Point> dstPoints, Function<Point,T> getPointIdentity) {
    dstPoints.stream().forEach(point -> {
      T id = getPointIdentity.apply(point);
      Optional<Point> srcPoint = srcPoints.stream()
        .filter(generated -> getPointIdentity.apply(generated).equals(id))
        .findFirst();
      
      if (srcPoint.isPresent()) {
        mergePointProperties(srcPoint.get(), point);
      }
    });
  }
  
  /**
   * Merges the properties of the src point into the dst point ONLY IF the dst point 
   * does not contain a value for the property.
   * @param src The source point where to copy the data
   * @param dst The destination point where to put the data
   */
  public static void mergePointProperties(Point src, Point dst) {
    Map<String, Object> dstProperties = dst.getProperties();
    Map<String, Object> srcProperties = src.getProperties();
    
    srcProperties.entrySet().stream()
      .filter(entry -> !dstProperties.containsKey(entry.getKey()))
      .forEach(entry -> dstProperties.put(entry.getKey(), entry.getValue()));
  }
}
