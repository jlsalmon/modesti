package cern.modesti.point;

import cern.modesti.schema.field.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.core.Relation;

import java.util.*;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Relation(value = "point", collectionRelation = "points")
@Data
@NoArgsConstructor
public class PointImpl implements Point {

  private static final long serialVersionUID = -7975036449830835583L;

  private Long lineNo;

  private boolean dirty = true;

  private boolean selected = false;

  private Boolean valid;

  private List<Error> errors = new ArrayList<>();

  private Map<String, Object> properties = new HashMap<>();

  public PointImpl(Long lineNo) {
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
   * Check to see if this {@link Point} is empty. A point is considered empty
   * if all the values of its properties are either null or empty string.
   *
   * @return true if the point was considered empty, false otherwise
   */
  @JsonIgnore
  public boolean isEmpty() {
    if (properties.size() == 0) {
      return true;
    }

    for (Object subProperty : properties.values()) {
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

  public List<Field> getEmptyFields(List<Field> fields) {
    List<Field> emptyFields = new ArrayList<>();

    for (Field field : fields) {
      Object value = getValueByPropertyName(field.getPropertyName());
      if (value == null || (value instanceof String && ((String) value).isEmpty())) {
        emptyFields.add(field);
      }

      // TODO: remove this domain-specific code
      // TODO: possibly replace with a "generated" field option which would cause it to be ignored
      // TODO: or a "multiple" option which would do the same... or both

      // HACK ALERT: treat auto-generated fields as "empty"
      if (field.getId().equals("tagname") || field.getId().equals("faultFamily") ||
          field.getId().equals("faultMember") || field.getId().equals("pointDescription")) {
        if (!emptyFields.contains(field)) {
          emptyFields.add(field);
        }
      }

      // HACK ALERT #2: ignore the monitoringEquipment field because it can be in multiple categories...
      if (field.getId().equals("monitoringEquipment")) {
        if (!emptyFields.contains(field)) {
          emptyFields.add(field);
        }
      }
    }

    return emptyFields;
  }

  /**
   * Get the value of the property of this point
   *
   * @param propertyName the name of the property to retrieve
   *
   * @return the corresponding value
   */
  public Object getValueByPropertyName(String propertyName) {
    Object value = null;

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
            java.lang.reflect.Field field = property.getClass().getDeclaredField(props[1]);
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

  @Override
  public boolean isValid() {
    return valid == null || !valid;
  }

  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Point)) return false;
    final PointImpl other = (PointImpl) o;
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
