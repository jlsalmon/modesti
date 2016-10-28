package cern.modesti.request.history;

import cern.modesti.request.point.Point;
import de.danielbechler.diff.identity.IdentityStrategy;

/**
 * @author Justin Lewis Salmon
 */
public class PointIdentityStrategy implements IdentityStrategy {

  @Override
  public boolean equals(Object working, Object base) {

    // FIXME: not all domains have a pointId field... how to determine identity?
    Long workingPointId = ((Point) working).getProperty("pointId", Long.class);
    Long basePointId = ((Point) base).getProperty("pointId", Long.class);

    return !(workingPointId == null || basePointId == null) && workingPointId.equals(basePointId);
  }
}
