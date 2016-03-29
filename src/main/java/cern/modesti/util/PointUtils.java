package cern.modesti.util;

import cern.modesti.request.point.Point;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public class PointUtils {

  /**
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

  /**
   * Utility method to convert a list of beans of a given type into a MODESTI
   * point object. The value of each field of the given type will be inserted
   * into the property map of the MODESTI point, where the key is the field
   * name and the value is the field value.
   *
   * @param pointsToCovert
   * @param klass
   * @param <T>
   * @return
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
