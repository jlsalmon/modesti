package cern.modesti.plugin.spi;

import cern.modesti.request.point.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Martin Flamm
 */
public interface SearchProvider extends ExtensionPoint {

  /**
   * A plugin should implement this method to provide search capability on its
   * domain database.
   *
   * @param query    an RSQL query string representing the user search input
   * @param pageable paging information
   *
   * @return a list of {@link Point} instances matching the given query
   */
  Page<Point> findAll(String query, Pageable pageable);
}