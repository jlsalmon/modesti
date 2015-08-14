package cern.modesti.util;

import cern.modesti.repository.jpa.subsystem.SubSystem;
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
  public static Request getTimRequest() {
    Request request = new Request();
    request.setRequestId("1");
    request.setType(RequestType.CREATE);
    request.setCreator(new User(1, "bert", "Bert", "Is Evil", "bert@modesti.ch", new HashSet<>(Collections.singleton(new Role("modesti-administrators")))));
    request.setDescription("description");
    request.setDomain("TIM");
    request.setSubsystem(new SubSystem(1L, "EAU DEMI", "EAU", "A", "DEMI", "B"));
    request.setCategories(new ArrayList<>(Collections.singletonList("PLC")));

    request.setPoints(getTimPoints());
    return request;
  }

  /**
   *
   * @return
   */
  public static Request getTimRequestWithAlarms() {
    Request request = getTimRequest();
    request.setRequestId("2");
    request.setPoints(getTimAlarms());
    return request;
  }

  /**
   *
   * @return
   */
  public static Request getTimRequestWithCabledPoints() {
    Request request = getTimRequest();
    request.setRequestId("3");
    request.setPoints(getTimCabledPoints());
    return request;
  }

  /**
   *
   * @return
   */
  public static Request getTimRequestWithCabledAlarms() {
    Request request = getTimRequest();
    request.setRequestId("4");
    request.setPoints(getTimCabledAlarms());
    return request;
  }

  /**
   *
   * @return
   */
  public static List<Point> getTimPoints() {
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
  public static List<Point> getTimAlarms() {
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
  private static List<Point> getTimCabledPoints() {
    List<Point> points = new ArrayList<>();

    Point point1 = new Point(1L);
    point1.setProperties(new HashMap<String, Object>() {{
      put("pointDescription", "TEST CABLED POINT 1");
      put("pointDataType", "Boolean");
      put("pointType", "APIMMD");
    }});

    points.add(point1);
    return points;
  }

  /**
   *
   * @return
   */
  private static List<Point> getTimCabledAlarms() {
    List<Point> points = new ArrayList<>();

    Point point1 = new Point(1L);
    point1.setProperties(new HashMap<String, Object>() {{
      put("pointDescription", "TEST CABLED ALARM 1");
      put("pointDataType", "Boolean");
      put("pointType", "APIMMD");
      put("priorityCode", 1);
      put("alarmValue", 0);
    }});

    points.add(point1);
    return points;
  }
}
