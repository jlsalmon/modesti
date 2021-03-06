package cern.modesti.request.history;

import cern.modesti.point.Point;
import de.danielbechler.diff.identity.IdentityStrategy;
import lombok.Data;

/**
 * @author Justin Lewis Salmon
 */
@Data
public class PointIdentityStrategy implements IdentityStrategy {

  private final String idProperty;

  public PointIdentityStrategy(String idProperty) {
    this.idProperty = idProperty;
  }

  @Override
  public boolean equals(Object working, Object base) {
    Object workingId = ((Point) working).getProperty(idProperty, Object.class);
    Object baseId = ((Point) base).getProperty(idProperty, Object.class);

    return !(workingId == null || baseId == null) && workingId.equals(baseId);
  }
}
