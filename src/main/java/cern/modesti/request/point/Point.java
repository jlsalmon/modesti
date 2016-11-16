package cern.modesti.request.point;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * This class represents a single data point. A
 * {@link cern.modesti.request.Request}. is composed of multiple points.
 * <p>
 * A point holds a map of arbitrary properties. These properties can be either
 * primitive values or complex objects.
 * <p>
 * In the case of complex object properties, the
 * {@link #getProperty(String, Class)} utility method can be used to
 * retrieve them as their specific domain class instance.
 * <p>
 * For example:
 * <p>
 * <code>
 * Point point = new Point();
 * point.addProperty("myDomainObject", new MyDomainObject());
 * MyDomainObject myDomainObject = point.getProperty("myDomainObject", MyDomainObject.class);
 * </code>
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Point implements Serializable {

  private static final long serialVersionUID = -7975036449830835583L;

  private Long lineNo;

  private Boolean dirty = true;

  private Boolean selected = false;

  private Boolean valid;

  private List<Error> errors = new ArrayList<>();

  private Map<String, Object> properties = new HashMap<>();

  public Point(Long lineNo) {
    this.lineNo = lineNo;
  }

  /**
   * Retrieve a point property and convert it to the given type.
   * <p>
   * The specific domain plugin is responsible for making sure that non
   * existent properties or properties of the wrong type are not requested.
   *
   * @param key   the property key
   * @param klass the type to which to convert the value
   * @param <T>   the type of the value
   * @return the value mapped by the given key, converted to the given type
   */
  public <T> T getProperty(String key, Class<T> klass) {
    Object value = properties.get(key);
    return new ObjectMapper().convertValue(value, klass);
  }

  /**
   * Retrieve a point property in the same way as
   * {@link #getProperty(String, Class)}, except that the provided default
   * value will be returned in case that the property value does not exist or
   * is null.
   *
   * @param key          the property key
   * @param klass        the type to which to convert the value
   * @param defaultValue the default value to return
   * @param <T>          he type of the value
   * @return the value mapped by the given key, converted to the given type or
   *         the provided default value
   */
  public <T> T getProperty(String key, Class<T> klass, T defaultValue) {
    T value = getProperty(key, klass);
    return value == null ? defaultValue : value;
  }

  /**
   * Add a property to the point.
   *
   * @param key   the property key
   * @param value the property value
   */
  public void addProperty(String key, Object value) {
    properties.put(key, value);
  }

  /**
   * Add an error message associated with a property of the point.
   * <p>
   * This method is designed to be used as a means of displaying validation
   * errors or other types of errors visually. The errors can be associated
   * directly with individual properties of the point.
   *
   * @param category the category to which the property belongs
   * @param property the property (map key) to associate the error with
   * @param message  the error message
   */
  public void addErrorMessage(String category, String property, String message) {
    boolean errorPropertyExists = false, propertyMessageExists = false;

    for (Error error : errors) {
      if (error.getProperty().equals(property)) {
        errorPropertyExists = true;

        for (String e : error.getErrors()) {
          if (e != null && e.equals(message)) {
            propertyMessageExists = true;
          }
        }

        if (!propertyMessageExists) {
          error.getErrors().add(message);
        }
      }
    }

    if (!errorPropertyExists) {
      Error error = new Error(category, property, new ArrayList<>(Collections.singletonList(message)));
      errors.add(error);
    }
  }

  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Point)) return false;
    final Point other = (Point) o;
    final Object this$lineNo = this.lineNo;
    final Object other$lineNo = other.lineNo;
    return !(this$lineNo == null ? other$lineNo != null : !this$lineNo.equals(other$lineNo));
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $lineNo = this.lineNo;
    result = result * PRIME + ($lineNo == null ? 0 : $lineNo.hashCode());
    final Object $dirty = this.dirty;
    result = result * PRIME + ($dirty == null ? 0 : $dirty.hashCode());
    final Object $selected = this.selected;
    result = result * PRIME + ($selected == null ? 0 : $selected.hashCode());
    final Object $errors = this.errors;
    result = result * PRIME + ($errors == null ? 0 : $errors.hashCode());
    final Object $properties = this.properties;
    result = result * PRIME + ($properties == null ? 0 : $properties.hashCode());
    return result;
  }
}
