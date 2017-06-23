package cern.modesti;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import cern.modesti.point.Point;
import cern.modesti.point.PointImpl;
import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import cern.modesti.request.RequestType;

/**
 * @author Justin Lewis Salmon
 */
public class TestUtil {

  public static Request getDummyRequest() {
    Request request = new RequestImpl();
    request.setType(RequestType.CREATE);
    request.setDescription("description");
    request.setDomain("DUMMY");
    request.setPoints(getDummyPoints());
    return request;
  }

  private static List<Point> getDummyPoints() {
    ArrayList<Point> points = new ArrayList<>();
    Point point1 = new PointImpl();
    Point point2 = new PointImpl();
    point1.setProperties(
        Maps.newHashMap(ImmutableMap.of("id", "1", "pointDescription", "TEST POINT 1", "pointDatatype", "Boolean")));
    point2.setProperties(
        Maps.newHashMap(ImmutableMap.of("id", "2", "pointDescription", "TEST POINT 2", "pointDatatype", "Boolean")));
    points.add(point1);
    points.add(point2);
    return points;
  }
}
