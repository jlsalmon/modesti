package cern.modesti.request.point;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

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
   * @param property
   * @param message
   */
  public void addErrorMessage(String property, String message) {
    boolean exists = false;

    for (Error error : errors) {
      if (error.getProperty().equals(property)) {
        exists = true;
        error.getErrors().add(message);
      }
    }

    if (!exists) {
      Error error = new Error(property, new ArrayList<>(Collections.singletonList(message)));
      errors.add(error);
    }
  }
}
