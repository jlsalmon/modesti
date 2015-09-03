package cern.modesti.validation;

import cern.modesti.repository.alarm.AlarmCategory;
import cern.modesti.repository.equipment.MonitoringEquipment;
import cern.modesti.repository.gmao.GmaoCode;
import cern.modesti.repository.location.BuildingName;
import cern.modesti.repository.location.Location;
import cern.modesti.repository.location.functionality.Functionality;
import cern.modesti.repository.location.zone.SafetyZone;
import cern.modesti.repository.person.Person;
import cern.modesti.repository.subsystem.SubSystem;
import cern.modesti.request.Request;
import cern.modesti.point.state.Error;
import cern.modesti.point.Point;
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
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static cern.modesti.util.Util.isEmptyPoint;
import static java.lang.String.format;

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

    // TODO: remove this when the validation procedure is ready
//    if (true) {
//      return true;
//    }

    // Delete all points with this request id
    String query = "DELETE FROM DRAFT_POINTS WHERE drp_request_id = ?";
    jdbcTemplate.update(query, request.getRequestId());

    for (Point point : request.getPoints()) {

      // Make a copy
      Map<String, Object> properties = new HashMap<>(point.getProperties());

      // Don't include empty points
      if (isEmptyPoint(point)) {
        continue;
      }

      // These are always required
      properties.put("requestId", request.getRequestId());
      properties.put("lineno", point.getId());

      // Some properties are stored as complex objects, so we need to retrieve the appropriate properties from them, ready for validation.

      properties.put("gmaoCode", properties.get("gmaoCode") != null ? ((GmaoCode) properties.get("gmaoCode")).getValue() : null);
      properties.put("respId", properties.get("responsiblePerson") != null ? ((Person) properties.get("responsiblePerson")).getId() : null);
      properties.remove("responsiblePerson");
      properties.put("subsystemId", properties.get("subsystem") != null ? ((SubSystem) properties.get("subsystem")).getId() : null);
      properties.remove("subsystem");
      properties.put("moneqId", properties.get("monitoringEquipment") != null ? ((MonitoringEquipment) properties.get("monitoringEquipment")).getId() : null);
      //properties.put("csamPlcname", getObjectProperty(properties, "csamPlcname", "value", String.class));
      properties.remove("monitoringEquipment");
      properties.put("csamCsename", properties.get("csamCsename") != null ? ((GmaoCode) properties.get("csamCsename")).getValue() : null);

      properties.put("buildingNumber", properties.get("location") != null ? ((Location) properties.get("location")).getBuildingNumber() : null);
      properties.put("buildingFloor", properties.get("location") != null ? ((Location) properties.get("location")).getFloor() : null);
      properties.put("buildingRoom", properties.get("location") != null ? ((Location) properties.get("location")).getRoom() : null);
      properties.remove("location");
      properties.put("buildingName", properties.get("buildingName") != null ? ((BuildingName) properties.get("buildingName")).getValue() : null);

      properties.put("funcCode", properties.get("functionality") != null ? ((Functionality) properties.get("functionality")).getValue() : null);
      properties.remove("functionality");
      properties.put("safetyZone", properties.get("safetyZone") != null ? ((SafetyZone) properties.get("safetyZone")).getValue() : null);

      properties.put("alarmCategory", properties.get("alarmCategory") != null ? ((AlarmCategory) properties.get("alarmCategory")).getValue() : null);
      //properties.put("priorityCode", Integer.valueOf(((String) properties.get("priorityCode")).split(":")[0]));

      // These properties do not go into the table
      // TODO: review these
      properties.remove("pointType");
      properties.remove("tagname");
      properties.remove("faultState");
      properties.remove("cabling");
      properties.remove("trueMeaning");
      properties.remove("falseMeaning");
      properties.remove("type");
      properties.remove("timeDeadband");
      properties.remove("userApplicationData");
      properties.remove("laserCategory");
      properties.remove("laserFaultFamily");
      properties.remove("laserFaultMember");
      properties.remove("laserFaultCode");
      properties.remove("parentAlarm");
      properties.remove("hostName");
      properties.remove("electricityFaultFamily");
      properties.remove("detail");
      properties.remove("multiplicityValue");
      properties.remove("csamDetector");

      // HACK ALERT: Loop over all property values and remove their descriptions (i.e. "4: Defaut centrale" -> "4")
      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        Object value = entry.getValue();
        if (value != null && String.class.isAssignableFrom(value.getClass())) {
          if (((String) entry.getValue()).contains(":")) {
            properties.put(entry.getKey(), ((String) entry.getValue()).split(":")[0]);
          }
        }
      }

      List<Object[]> data = new ArrayList<>();
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
    call.addDeclaredParameter(new SqlParameter("p_request_stage", OracleTypes.VARCHAR));
    call.addDeclaredParameter(new SqlOutParameter("p_exitcode", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlOutParameter("p_exittext", OracleTypes.VARCHAR));

    Map<String, Object> results = call.execute(Integer.valueOf(request.getRequestId()), request.getCreator().getEmployeeId(), request.getStatus().toString());
    log.debug(format("exitcode: %s, exittext: %s", results.get("p_exitcode"), results.get("p_exittext")));
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
    log.debug(format("converted property name %s to column name %s", property, columnName));
    return columnName;
  }
}
