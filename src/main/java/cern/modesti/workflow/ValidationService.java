package cern.modesti.workflow;

import cern.modesti.repository.jpa.validation.DraftPoint;
import cern.modesti.repository.jpa.validation.ValidationRepository;
import cern.modesti.repository.jpa.validation.ValidationResult;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
   *
   * @param request
   * @return
   */
  public ValidationResult validateRequest(Request request) {
    List<DraftPoint> draftPoints = new ArrayList<>();

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();
      DraftPoint draftPoint = new DraftPoint(Long.valueOf(request.getRequestId()), point.getId(),
          (String) properties.get("pointDataType"),
          (String) properties.get("pointDescription"),
          (String) properties.get("gmaoCode"),
          (String) properties.get("otherCode"));

      draftPoints.add(draftPoint);
    }

    // Delete everything from the table
    repository.deleteAll();
    // Write the points
    repository.save(draftPoints);
    // Call the stored procedure
    ValidationResult result = repository.validate(request);
    // Read the points back to get the exit codes and error messages
    Iterable<DraftPoint> validatedPoints = repository.findAll();

    LOG.debug("result exit: (" + result.getExitcode() + ") " + result.getExittext());
    for (DraftPoint draftPoint : validatedPoints) {
      LOG.debug("draft point exit: (" + draftPoint.getExitCode() + ") " + draftPoint.getExitText());
    }

    return result;
  }
}
