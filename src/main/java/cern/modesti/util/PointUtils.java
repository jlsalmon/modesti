package cern.modesti.util;

import cern.modesti.request.point.Point;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Utility class to ease working with {@link Point} objects.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
public class PointUtils {

  /**
   * Check to see if the given {@link Point} is empty. A point is considered
   * empty if all the values of its properties are either null or empty string.
   *
   * @param point the point object
   * @return true if the point was considered empty, false otherwise
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

  /**
   * Get the value of the property of a point
   *
   * @param point the
   * @param propertyName
   * @return
   */
  public static Object getValueByPropertyName(Point point, String propertyName) {
    Object value = null;
    Map<String, Object> properties = point.getProperties();

    if (propertyName.contains(".")) {
      String[] props = propertyName.split("\\.");

      if (properties.containsKey(props[0])) {
        Object property = properties.get(props[0]);

        if (property == null) {
          return null;
        }

        if (property instanceof Map) {
          value = ((Map) property).get(props[1]);
        } else {
          try {
            Field field = property.getClass().getDeclaredField(props[1]);
            field.setAccessible(true);
            value = field.get(property);
          } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(format("Error introspecting point property of type %s", property.getClass()), e);
          }
        }
      }

    } else {
      if (properties.containsKey(propertyName)) {
        value = properties.get(propertyName);
      }
    }

    return value;
  }

  /**
   * Utility method to convert a list of beans of a given type into a MODESTI
   * point object. The value of each field of the given type will be inserted
   * into the property map of the MODESTI point, where the key is the field
   * name and the value is the field value.
   *
   * @param pointsToCovert list of objects to be converted
   * @param klass          the type to convert from
   * @param <T>            the type to convert from
   * @return a list of {@link Point} instances created from the given objects
   */
  public static <T> List<Point> convertPoints(Iterable<T> pointsToCovert, Class<T> klass) {
    List<Point> points = new ArrayList<>();
    BeanInfo beanInfo;

    try {
      beanInfo = Introspector.getBeanInfo(klass);
    } catch (IntrospectionException e) {
      log.error(format("Error introspecting point of type %s", klass), e);
      throw new RuntimeException(e);
    }

    for (T t : pointsToCovert) {
      Point point = new Point();

      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        String propertyName = propertyDescriptor.getName();
        if (propertyName.equals("class")) {
          continue;
        }

        Object value;

        try {
          value = propertyDescriptor.getReadMethod().invoke(t);
          Class<?> type = propertyDescriptor.getPropertyType();

          // This is a hack to make sure that all Object properties are constructed with null fields inside
          // because handsontable craps out otherwise
          if (value == null && !type.isPrimitive() && !type.equals(String.class) && !type.equals(Integer.class)
              && !type.equals(Long.class) && !type.equals(Class.class)) {

            value = propertyDescriptor.getPropertyType().newInstance();
          }

        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
          log.error(format("Error converting point of type %s", klass), e);
          throw new RuntimeException(e);
        }

        point.addProperty(propertyName, value);
      }

      points.add(point);
    }

    return points;
  }
}
