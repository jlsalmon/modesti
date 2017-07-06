package cern.modesti.point;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class PointConverterImpl implements PointConverter {

  @Override
  public <T> Page<Point> convert(Page<T> pointsToConvert, Class<T> klass, Pageable pageable) {
    List<Point> points = new ArrayList<>();
    BeanInfo beanInfo;

    try {
      beanInfo = Introspector.getBeanInfo(klass);
    } catch (IntrospectionException e) {
      log.error(format("Error introspecting point of type %s", klass), e);
      throw new RuntimeException(e);
    }

    for (T t : pointsToConvert) {
      Point point = new PointImpl();

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
              && !type.equals(Long.class) && !type.equals(Class.class) && !type.equals(Float.class)) {

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

    return new PageImpl<>(points, pageable, pointsToConvert.getTotalElements());
  }
}
