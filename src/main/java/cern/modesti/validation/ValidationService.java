package cern.modesti.validation;

import cern.modesti.repository.jpa.validation.DraftPoint;
import cern.modesti.repository.jpa.validation.ValidationRepository;
import cern.modesti.request.Request;
import cern.modesti.request.point.Error;
import cern.modesti.request.point.Point;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ValidationService {

  @Autowired
  ValidationRepository repository;

  /**
   * @param request
   *
   * @return true if the request is valid, false otherwise. When false, error messages will be attached to each individual point and be retrievable via
   * {@link Point#getErrors()}.
   */
  public boolean validateRequest(Request request) {
    List<DraftPoint> draftPoints = new ArrayList<>();

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();

      // Don't include empty points
      if (isEmptyPoint(point)) {
        continue;
      }

      // Create a DraftPoint object. Pull out all the objects into their scalar values ready for validation. Unfortunately the JSON object doesn't give us
      // the Functionality, Location, Subsystem etc. objects back, but Maps instead. So we have to cast. Not sure of the best way to solve that problem.
      DraftPoint draftPoint = new DraftPoint(Long.valueOf(request.getRequestId()), point.getId(),

          // General
          getProperty(properties, "pointDatatype", String.class),
          getProperty(properties, "pointDescription", String.class),
          getObjectProperty(properties, "gmaoCode", "value", String.class),
          getProperty(properties, "otherCode", String.class),
          getProperty(properties, "pointAttribute", String.class),
          getObjectProperty(properties, "responsiblePerson", "id", Integer.class),
          getObjectProperty(properties, "subsystem", "id", Integer.class),
          getObjectProperty(properties, "monitoringEquipment", "id", Integer.class),
          getProperty(properties, "pointComplementaryInfo", String.class),

          // Location
          getObjectProperty(properties, "buildingName", "value", String.class),
          getObjectProperty(properties, "location", "buildingNumber", String.class),
          getObjectProperty(properties, "location", "floor", String.class),
          getObjectProperty(properties, "location", "room", String.class),
          getObjectProperty(properties, "functionality", "value", String.class),
          getObjectProperty(properties, "zone", "value", String.class),

          // Alarms
          getProperty(properties, "alarmValue", Integer.class),
          getProperty(properties, "priorityCode", Integer.class),
          getObjectProperty(properties, "alarmCategory", "value", String.class),

          // Alarm Help
          getProperty(properties, "alarmCauses", String.class),
          getProperty(properties, "alarmConsequences", String.class),
          getProperty(properties, "workHoursTask", String.class),
          getProperty(properties, "outsideHoursTask", String.class),

          // PLC (APIMMD)
          getProperty(properties, "blockType", Integer.class),
          getProperty(properties, "wordId", Integer.class),
          getProperty(properties, "bitId", Integer.class),
          getProperty(properties, "nativePrefix", String.class),
          getProperty(properties, "slaveAddress", Integer.class),
          getProperty(properties, "connectId", String.class),

          // Exit parameters
          null, null
      );

      draftPoints.add(draftPoint);
    }

    // Delete all points with this request id.
    List<DraftPoint> oldPoints = repository.findByRequestId(Long.valueOf(request.getRequestId()));
    repository.delete(oldPoints);

    // Write the points
    repository.save(draftPoints);
    // Call the stored procedure
    boolean valid = repository.validate(request);

    if (!valid) {
      // Read the points back to get the exit codes and error messages
      draftPoints = repository.findByRequestId(Long.valueOf(request.getRequestId()));
      List<Point> points = request.getPoints();

      for (DraftPoint draftPoint : draftPoints) {
        log.debug("draft point exit: " + draftPoint);

        // Set the error messages on the points
        for (Point point : points) {
          if (point.getId().equals(draftPoint.getLineNumber())) {
            Long exitCode = draftPoint.getExitCode();
            String exitText = draftPoint.getExitText();

            if (exitCode != null && exitCode > 0) {
              point.setErrors(Collections.singletonList(new Error("", Collections.singletonList(exitText != null ? exitText : "unknown error"))));
            }
          }
        }
      }
    }

    return valid;
  }

  /**
   *
   * @param properties
   * @param property
   * @param klass
   * @param <T>
   * @return
   */
  private <T> T getProperty(Map<String, Object> properties, String property, Class<T> klass) {
    return klass.cast(properties.get(property));
  }

  /**
   *
   * @param properties
   * @param objectName
   * @param property
   * @param klass
   * @param <T>
   * @return
   */
  private <T> T getObjectProperty(Map<String, Object> properties, String objectName, String property, Class<T> klass) {
    Object o = null;
    T t = null;

    Map map = (Map) properties.get(objectName);
    if (map != null) {
      o = map.get(property);
    }

    if (o != null) {
      t = klass.cast(o);
    }

    return t;
  }

  /**
   *
   * @param property
   * @return
   */
  private String propertyToColumnName(String property) {
    String columnName = "drp_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property);
    log.debug(String.format("converted property name %s to column name %s", property, columnName));
    return columnName;
  }

  /**
   *
   * @param point
   * @return
   */
  private boolean isEmptyPoint(Point point) {
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
}
