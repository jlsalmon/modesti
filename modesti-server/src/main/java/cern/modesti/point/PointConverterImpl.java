package cern.modesti.point;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

/**
 * TODO: Move to modesti-api to share with tim-configurator
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class PointConverterImpl implements PointConverter {

  @Override
  public <T> Page<Point> convert(Page<T> pointsToConvert, Class<T> klass, Pageable pageable) {
    List<Point> points = new ArrayList<>();

    for (T pointToConvert : pointsToConvert) {
      points.add(convert(pointToConvert, klass));
    }

    return new PageImpl<>(points, pageable, pointsToConvert.getTotalElements());
  }

  @Override
  public <T> List<Point> convert(List<T> pointsToConvert, Class<T> klass) {
    List<Point> points = Collections.synchronizedList(new ArrayList<>());
    pointsToConvert.stream().parallel().forEach(t -> points.add(convert(t, klass)));
    return points;
  }

  /**
   * Utility method to convert a bean of a given type into MODESTI
   * point objects. The value of each field of the given type will be inserted
   * into the property map of the MODESTI point, where the key is the field
   * name and the value is the field value.
   *
   * @param pointToConvert  object to be converted
   * @param klass           the type to convert from
   * @param <T>             the type to convert from
   *
   * @return a list of {@link Point} instances created from the given objects
   */
  public static <T> Point convert(T pointToConvert, Class<T> klass) {
    Point point = new PointImpl();

    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(klass);
    } catch (IntrospectionException e) {
      log.error(format("Error introspecting point of type %s", klass), e);
      throw new RuntimeException(e);
    }

    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      if ("class".equals(propertyName)) {
        continue;
      }

      Object value;

      try {
        value = propertyDescriptor.getReadMethod().invoke(pointToConvert);
        Class<?> type = propertyDescriptor.getPropertyType();

        // This is a hack to make sure that all Object properties are constructed with null fields inside
        // because handsontable craps out otherwise
        if (value == null && !type.isPrimitive() && !type.equals(String.class) && !type.equals(Integer.class)
            && !type.equals(Long.class) && !type.equals(Class.class)  && !type.equals(Float.class)) {

          value = propertyDescriptor.getPropertyType().newInstance();
        }

      } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
        log.error(format("Error converting property %s of point type %s", propertyName, klass), e);
        throw new RuntimeException(e);
      }

      point.addProperty(propertyName, value);
    }
    return point;
  }
}
