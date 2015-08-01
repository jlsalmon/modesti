package cern.modesti.validation;

import cern.modesti.request.Request;
import cern.modesti.request.point.Error;
import cern.modesti.request.point.Point;
import com.google.common.base.CaseFormat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

import static cern.modesti.util.Util.isEmptyPoint;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ValidationService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * @param request
   *
   * @return true if the request is valid, false otherwise. When false, error messages will be attached to each individual point and be retrievable via
   * {@link Point#getErrors()}.
   */
  @Transactional
  public boolean validateRequest(Request request) {
    // Delete all points with this request id
    String query = "DELETE FROM DRAFT_POINTS WHERE drp_request_id = ?";
    jdbcTemplate.update(query, request.getRequestId());

    for (Point point : request.getPoints()) {
      List<Object[]> data = new ArrayList<>();

      // Make a copy
      Map<String, Object> properties = new HashMap<>(point.getProperties());

      // Don't include empty points
      if (isEmptyPoint(point)) {
        continue;
      }

      // These are always required
      properties.put("requestId", request.getRequestId());
      properties.put("lineno", point.getId());

      // Need to hack out the object properties into their scalar values ready for validation. Unfortunately the JSON object doesn't give us
      // the Functionality, Location, Subsystem etc. objects back, but Maps instead. So we have to cast. Not sure of the best way to solve that problem.
      // If the object properties were flattened on the client side, this wouldn't be necessary.
      properties.put("gmaoCode", getObjectProperty(properties, "gmaoCode", "value", String.class));
      properties.put("respId", getObjectProperty(properties, "responsiblePerson", "id", Integer.class));
      properties.remove("responsiblePerson");
      properties.put("subsystemId", getObjectProperty(properties, "subsystem", "id", Integer.class));
      properties.remove("subsystem");
      properties.put("moneqId", getObjectProperty(properties, "monitoringEquipment", "id", Integer.class));
      properties.put("csamPlcname", getObjectProperty(properties, "csamPlcname", "value", String.class));
      properties.remove("monitoringEquipment");
      properties.put("buildingName", getObjectProperty(properties, "buildingName", "value", String.class));
      properties.put("buildingNumber", getObjectProperty(properties, "location", "buildingNumber", String.class));
      properties.put("buildingFloor", getObjectProperty(properties, "location", "floor", String.class));
      properties.put("buildingRoom", getObjectProperty(properties, "location", "room", String.class));
      properties.remove("location");
      properties.put("funcCode", getObjectProperty(properties, "functionality", "value", String.class));
      properties.remove("functionality");
      properties.put("safetyZone", getObjectProperty(properties, "safetyZone", "value", String.class));
      properties.put("alarmCategory", getObjectProperty(properties, "alarmCategory", "value", String.class));

      // These properties do not go into the table
      properties.remove("tagname");
      properties.remove("faultState");
      properties.remove("cabling");
      properties.remove("trueMeaning");
      properties.remove("falseMeaning");
      properties.remove("type");

      properties.remove("csamDetector");

      data.add(properties.values().toArray());

      // Create an insert statement based on the properties we have in this point. This is possible due to the standardisation of column names in the
      // DRAFT_POINTS table (See https://edms.cern.ch/document/1506060/1)
      query = "INSERT INTO DRAFT_POINTS (";
      query += properties.keySet().stream().map(s -> s = propertyToColumnName(s)).collect(Collectors.joining(", "));
      query += ") VALUES (";
      query += properties.values().stream().map(s -> "?").collect(Collectors.joining(", "));
      query += ")";
      log.debug(query);

      // Uses JdbcTemplate's batchUpdate operation to bulk load data
      jdbcTemplate.batchUpdate(query, data);
    }

    // Call the stored procedure
    validate(request);

    // Read the points back to get the exit codes and error messages
    query = "SELECT drp_request_id, drp_lineno, drp_exitcode, drp_exittext FROM DRAFT_POINTS WHERE drp_request_id = ?";

    boolean valid = true;
    List<Result> results = jdbcTemplate.query(query, new Object[]{request.getRequestId()}, (rs, rowNum) -> new Result(rs.getInt("drp_request_id"), rs.getInt
        ("drp_lineno"), rs.getInt("drp_exitcode"), rs.getString("drp_exittext")));

    for (Result result : results) {
      if (result.getExitCode() != 0) {
        valid = false;
        setErrorMessage(request, result);
      }
    }

    return valid;
  }

  /**
   * @param request
   */
  private void validate(Request request) {
    log.debug("validating via stored procedure");

    SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withCatalogName("TIMPKREQCHECK").withProcedureName("STP_CHECK_REQUEST");
    call.addDeclaredParameter(new SqlParameter("p_request_id", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlParameter("p_user_id", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlOutParameter("p_exitcode", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlOutParameter("p_exittext", OracleTypes.VARCHAR));

    call.execute(Integer.valueOf(request.getRequestId()), request.getCreator().getId());
  }

  /**
   * @param request
   * @param result
   */
  private void setErrorMessage(Request request, Result result) {
    log.debug("draft point exit: " + result.toString());

    // Set the error messages on the points
    for (Point point : request.getPoints()) {
      if (point.getId().equals(new Long(result.getLineno()))) {
        Integer exitCode = result.getExitCode();
        String exitText = result.getExitText();

        if (exitCode != null && exitCode > 0) {
          point.setErrors(Collections.singletonList(new Error("", Collections.singletonList(exitText != null ? exitText : "unknown error"))));
        }
      }
    }
  }

  @Data
  class Result {
    final Integer requestId;
    final Integer lineno;
    final Integer exitCode;
    final String exitText;
  }

  /**
   * @param properties
   * @param objectName
   * @param property
   * @param klass
   * @param <T>
   *
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
   * @param property
   *
   * @return
   */
  private String propertyToColumnName(String property) {
    String columnName = "drp_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property);
    log.debug(String.format("converted property name %s to column name %s", property, columnName));
    return columnName;
  }
}
