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
 * Implementation of {@link Point} representing a single data point
 * 
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

  /**
   * Class constructor
   * @param lineNo Line number of the point definition
   */
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
  @Override
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
  @Override
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
  @Override
  public void addProperty(String key, Object value) {
    properties.put(key, value);
  }

  /**
   * Check to see if this {@link Point} is empty. A point is considered empty
   * if all the values of its properties are either null or empty string.
   *
   * @return true if the point was considered empty, false otherwise
   */
  @Override
  @JsonIgnore
  public boolean isEmpty() {
    if (properties.size() == 0) {
      return true;
    }

    for (Object subProperty : properties.values()) {
      if (subProperty instanceof Map) {
        for (Object subSubProperty : ((Map) subProperty).values()) {
          if (subSubProperty != null && !"".equals(subSubProperty)) {
            return false;
          }
        }
      } else if (subProperty != null && !"".equals(subProperty)) {
        return false;
      }
    }

    return true;
  }

  @Override
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
      if ( ("tagname".equals(field.getId()) || "faultFamily".equals(field.getId()) ||
          "faultMember".equals(field.getId()) || "pointDescription".equals(field.getId())) && 
          !emptyFields.contains(field)) {
        emptyFields.add(field);
      }

      // HACK ALERT #2: ignore the monitoringEquipment field because it can be in multiple categories...
      if ("monitoringEquipment".equals(field.getId()) && !emptyFields.contains(field)) {
        emptyFields.add(field);
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
  @Override
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
  @Override
  public void addErrorMessage(String category, String property, String message) {
    String finalMessageToLog = Optional.ofNullable(message)
            .orElse("Unexpected error occurred, no message available. Try again in few minutes.");
    boolean errorPropertyExists = false;
    boolean propertyMessageExists = false;

    for (Error error : errors) {
      if (error.getProperty().equals(property)) {
        errorPropertyExists = true;

        for (String e : error.getErrors()) {
          if (e != null && e.equals(finalMessageToLog)) {
            propertyMessageExists = true;
          }
        }

        if (!propertyMessageExists) {
          error.getErrors().add(finalMessageToLog);
        }
      }
    }

    if (!errorPropertyExists) {
      Error error = new Error(category, property, new ArrayList<>(Collections.singletonList(finalMessageToLog)));
      errors.add(error);
    }
  }

  @Override
  public boolean isValid() {
    return valid == null || !valid;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Point)) return false;
    final PointImpl other = (PointImpl) o;
    final Object thisLineNo = this.lineNo;
    final Object otherLineNo = other.lineNo;
    return !(thisLineNo == null ? otherLineNo != null : !thisLineNo.equals(otherLineNo));
  }

  @Override
  public int hashCode() {
    final int prime = 59;
    int result = 1;
    final Object thisLineNo = this.lineNo;
    result = result * prime + (thisLineNo == null ? 0 : thisLineNo.hashCode());
    final Object thisDirty = this.dirty;
    result = result * prime + (thisDirty == null ? 0 : thisDirty.hashCode());
    final Object thisSelected = this.selected;
    result = result * prime + (thisSelected == null ? 0 : thisSelected.hashCode());
    final Object thisErrors = this.errors;
    result = result * prime + (thisErrors == null ? 0 : thisErrors.hashCode());
    final Object thisProperties = this.properties;
    result = result * prime + (thisProperties == null ? 0 : thisProperties.hashCode());
    return result;
  }
}
