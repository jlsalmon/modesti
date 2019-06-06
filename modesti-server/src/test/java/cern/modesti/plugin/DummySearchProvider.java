package cern.modesti.plugin;

import cern.modesti.plugin.spi.SearchProvider;
import cern.modesti.point.Point;
import cern.modesti.point.PointConverter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Justin Lewis Salmon
 */
public class DummySearchProvider implements SearchProvider {

  @Override
  public Page<Point> findAll(String query, Pageable pageable, PointConverter converter) {
    return null;
  }

  @Override
  public String getPluginId() {
    return DummyRequestProvider.DUMMY;
  }
}
