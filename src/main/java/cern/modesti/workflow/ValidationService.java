package cern.modesti.workflow;

import cern.modesti.repository.jpa.validation.DraftPoint;
import cern.modesti.repository.jpa.validation.ValidationRepository;
import cern.modesti.request.Request;
import cern.modesti.request.point.Error;
import cern.modesti.request.point.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
      DraftPoint draftPoint = new DraftPoint(Long.valueOf(request.getRequestId()), point.getId(), (String) properties.get("pointDataType"), (String)
          properties.get("pointDescription"), (String) properties.get("gmaoCode"), (String) properties.get("otherCode"));

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
            if (draftPoint.getExitCode() > 0) {
              point.setErrors(Collections.singletonList(new Error("", Collections.singletonList(draftPoint.getExitText()))));
            }
          }
        }
      }
    }

    return valid;
  }
}
