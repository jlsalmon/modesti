package cern.modesti.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Justin Lewis Salmon
 */
public interface PointConverter {

  /**
   * Utility method to convert a list of beans of a given type into MODESTI
   * point objects. The value of each field of the given type will be inserted
   * into the property map of the MODESTI point, where the key is the field
   * name and the value is the field value.
   *
   * @param pointsToConvert page of objects to be converted
   * @param klass           the type to convert from
   * @param pageable        paging information
   * @param <T>             the type to convert from
   *
   * @return a page of {@link Point} instances created from the given objects
   */
  <T> Page<Point> convert(Page<T> pointsToConvert, Class<T> klass, Pageable pageable);
}
