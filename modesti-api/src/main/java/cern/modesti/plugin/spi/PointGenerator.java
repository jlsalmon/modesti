package cern.modesti.plugin.spi;

import java.util.List;

import cern.modesti.point.Point;

/**
 * Generate a list of {@link cern.modesti.point.Point} from the point IDs
 * 
 * @author Ivan Prieto Barreiro
 */
public interface PointGenerator {

  /**
   * Find all the points which id is provided as parameter
   * @param pointIds list of point ids to search for
   * @return List of {@link Point} found in the database
   */
  List<Point> findAllByPointId(List<Long> pointIds);
}
