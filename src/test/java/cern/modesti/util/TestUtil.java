package cern.modesti.util;

import cern.modesti.repository.subsystem.SubSystem;
import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import cern.modesti.request.point.Point;
import cern.modesti.security.ldap.Role;
import cern.modesti.security.ldap.User;

import java.util.*;

/**
 *
 */
public class TestUtil {

  /**
   *
   * @return
   */
  public static Request getDefaultRequest() {
    Request request = new Request();
    request.setRequestId("1");
    request.setType(RequestType.CREATE);
    request.setCreator(new User(1, "bert", "Bert", "Is Evil", "bert@modesti.ch", new HashSet<>(Collections.singleton(new Role("modesti-administrators")))));
    request.setDescription("description");
    request.setDomain("DEFAULT");
    request.setSubsystem(new SubSystem(1L, "EAU DEMI", "EAU", "A", "DEMI", "B"));
    request.setCategories(new HashSet<>(Collections.singletonList("PLC")));

    request.setPoints(getDefaultPoints());
    return request;
  }

  /**
   *
   * @return
   */
  public static Request getDefaultRequestWithAlarms() {
    Request request = getDefaultRequest();
    request.setRequestId("2");
    request.setPoints(getDefaultAlarms());
    return request;
  }

  /**
   *
   * @return
   */
  public static Request getDefaultRequestWithCabledPoints() {
    Request request = getDefaultRequest();
    request.setRequestId("3");
    request.setPoints(getDefaultCabledPoints());
    return request;
  }

  /**
   *
   * @return
   */
  public static Request getDefaultRequestWithCabledAlarms() {
    Request request = getDefaultRequest();
    request.setRequestId("4");
    request.setPoints(getDefaultCabledAlarms());
    return request;
  }

  /**
   *
   * @return
   */
  public static List<Point> getDefaultPoints() {
    List<Point> points = new ArrayList<>();

    Point point1 = new Point(1L);
    point1.setProperties(new HashMap<String, Object>() {{
      put("pointDescription", "TEST POINT 1");
      put("pointDataType", "Boolean");
    }});

    points.add(point1);
    return points;
  }

  /**
   *
   * @return
   */
  public static List<Point> getDefaultAlarms() {
    List<Point> points = new ArrayList<>();

    Point point1 = new Point(1L);
    point1.setProperties(new HashMap<String, Object>() {{
      put("pointDescription", "TEST ALARM 1");
      put("pointDataType", "Boolean");
      put("priorityCode", 1);
      put("alarmValue", 0);
    }});

    points.add(point1);
    return points;
  }

  /**
   *
   * @return
   */
  private static List<Point> getDefaultCabledPoints() {
    List<Point> points = new ArrayList<>();

    Point point1 = new Point(1L);
    point1.setProperties(new HashMap<String, Object>() {{
      put("pointDescription", "TEST CABLED POINT 1");
      put("pointDataType", "Boolean");
      put("pointType", "PLC");
    }});

    points.add(point1);
    return points;
  }

  /**
   *
   * @return
   */
  private static List<Point> getDefaultCabledAlarms() {
    List<Point> points = new ArrayList<>();

    Point point1 = new Point(1L);
    point1.setProperties(new HashMap<String, Object>() {{
      put("pointDescription", "TEST CABLED ALARM 1");
      put("pointDataType", "Boolean");
      put("pointType", "PLC");
      put("priorityCode", 1);
      put("alarmValue", 0);
    }});

    points.add(point1);
    return points;
  }
}
