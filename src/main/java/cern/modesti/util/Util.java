package cern.modesti.util;

import cern.modesti.point.Point;

import java.util.Map;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class Util {

  /**
   *
   * @param point
   * @return
   */
  public static boolean isEmptyPoint(Point point) {
    if (point.getProperties().size() == 0) {
      return true;
    }

    for (Object subProperty : point.getProperties().values()) {
      if (subProperty instanceof Map) {
        for (Object subSubProperty : ((Map) subProperty).values()) {
          if (subSubProperty != null && !subSubProperty.equals("")) {
            return false;
          }
        }
      } else if (subProperty != null && !subProperty.equals("")) {
        return false;
      }
    }

    return true;
  }
}
