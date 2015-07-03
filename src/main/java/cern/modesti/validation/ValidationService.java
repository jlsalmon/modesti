package cern.modesti.validation;

import cern.modesti.repository.jpa.validation.DraftPoint;
import cern.modesti.repository.jpa.validation.ValidationRepository;
import cern.modesti.request.Request;
import cern.modesti.request.point.Error;
import cern.modesti.request.point.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
public class ValidationService {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationService.class);

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

      // Unfortunately the JSON object doesn't give us the Site, Location, Subsystem etc. objects back, but Maps instead.
      // Not sure of the best way to solve that problem.
      String gmaoCode = (String) ((Map) properties.get("gmaoCode")).get("value");
      String functionalityCode = (String) ((Map) properties.get("site")).get("value");
      String buildingName = (String) ((Map) properties.get("buildingName")).get("value");
      String buildingNumber = (String) ((Map) properties.get("location")).get("buildingNumber");
      String buildingFloor = (String) ((Map) properties.get("location")).get("floor");
      String buildingRoom = (String) ((Map) properties.get("location")).get("room");
      Integer responsibleId = (Integer) ((Map) properties.get("responsiblePerson")).get("id");
      Integer subsystemId = (Integer) ((Map) properties.get("subsystem")).get("id");
      Integer monitoringEquipmentId = (Integer) ((Map) properties.get("monitoringEquipment")).get("id");


      DraftPoint draftPoint = new DraftPoint(Long.valueOf(request.getRequestId()), point.getId(),
          (String) properties.get("pointDataType"),
          (String) properties.get("pointDescription"),
          gmaoCode,
          (String) properties.get("otherCode"),
          functionalityCode,
          buildingName,
          buildingNumber,
          buildingFloor,
          buildingRoom,
          (String) properties.get("pointAttribute"),
          responsibleId,
          subsystemId,
          monitoringEquipmentId);

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
        LOG.debug("draft point exit: " + draftPoint);

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
