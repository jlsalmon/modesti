package cern.modesti.plugin.spi;

import cern.modesti.point.Point;
import cern.modesti.point.PointConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Martin Flamm
 * @author Justin Lewis Salmon
 */
public interface SearchProvider extends ExtensionPoint {

  /**
   * A plugin should implement this method to provide search capability on its
   * domain database.
   *
   * @param query     an RSQL query string representing the user search input
   * @param pageable  paging information
   * @param converter utility component for converting domain-specific entity
   *                  objects to {@link Point} instances
   *
   * @return a list of {@link Point} instances matching the given query
   */
  Page<Point> findAll(String query, Pageable pageable, PointConverter converter);
}
