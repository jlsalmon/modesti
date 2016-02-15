package cern.modesti.request.point;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Point {

  private Long lineNo;

  private Boolean dirty = true;

  private Boolean selected = false;

  private List<Error> errors = new ArrayList<>();

  private Map<String, Object> properties = new HashMap<>();

  public Point(Long lineNo) {
    this.lineNo = lineNo;
  }

  /**
   * @param key
   * @param klass
   * @param <T>
   * @return
   */
  public <T> T getObjectProperty(String key, Class<T> klass) {
    Object value = properties.get(key);
    return new ObjectMapper().convertValue(value, klass);
  }

  /**
   *
   * @param key
   * @param value
   */
  public void addProperty(String key, Object value) {
    properties.put(key, value);
  }

  /**
   * @param property
   * @param message
   */
  public void addErrorMessage(String property, String message) {
    boolean errorPropertyExists = false, propertyMessageExists = false;

    for (Error error : errors) {
      if (error.getProperty().equals(property)) {
        errorPropertyExists = true;

        for (String e : error.getErrors()) {
          if (e.equals(message)) {
            propertyMessageExists = true;
          }
        }

        if (!propertyMessageExists) {
          error.getErrors().add(message);
        }
      }
    }

    if (!errorPropertyExists) {
      Error error = new Error(property, new ArrayList<>(Collections.singletonList(message)));
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
