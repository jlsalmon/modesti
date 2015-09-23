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
import cern.modesti.request.point.state.Error;
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

import javax.transaction.Transactional;
import java.math.BigDecimal;
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

  /**
   * @param request
   *
   * @return true if the request is valid, false otherwise. When false, error messages will be attached to each individual point and be retrievable via
   * {@link Point#getErrors()}.
   */
  @Transactional
  public boolean validateRequest(Request request) {

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
      properties.put("lineno", point.getLineNo());

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

      /**
       * DRP_STATEPT_DESCLIST : ON,OFF
       */
      if (properties.get("trueMeaning") != null && properties.get("falseMeaning") != null) {
        properties.put("stateptDesclist", properties.get("trueMeaning") + "," + properties.get("falseMeaning"));
        properties.remove("trueMeaning");
        properties.remove("falseMeaning");
      }

      // These properties do not go into the table
      // TODO: review these
      properties.remove("pointType");
      properties.remove("tagname");
      properties.remove("faultState");
      properties.remove("cabling");
      properties.remove("type");
      properties.remove("timeDeadband");
      properties.remove("csamDetector");

      // Deprecated properties
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
    Map<String, Object> exit = validate(request);

    boolean valid = true;
    Long exitcode = ((BigDecimal) exit.get("p_exitcode")).longValue();

    if (exitcode != 0) {
      valid = false;

      // Read the points back to get the error messages
      query = "SELECT mer_request_id, mer_lineno, mer_error_text, mer_column_name FROM MODESTI_REQ_ERRORS WHERE mer_request_id = ?";

      List<ErrorMessage> errorMessages = jdbcTemplate.query(query, new Object[]{request.getRequestId()}, (rs, rowNum) -> new ErrorMessage(rs.getInt
          ("mer_request_id"), rs.getInt("mer_lineno"), rs.getString("mer_error_text"), rs.getString("mer_column_name")));

      for (ErrorMessage errorMessage : errorMessages) {
        Point point = request.getPoints().get(errorMessage.getLineno() - 1);
        point.setValid(false);
        setErrorMessage(point, errorMessage);
      }
    }

    return valid;
  }

  /**
   *
   * @param request
   * @return
   */
  private Map<String, Object> validate(Request request) {
    log.debug("validating via stored procedure");

    SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate).withCatalogName("TIMPKREQCHECK").withProcedureName("STP_CHECK_REQUEST");
    call.addDeclaredParameter(new SqlParameter("p_request_id", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlParameter("p_user_id", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlParameter("p_request_stage", OracleTypes.VARCHAR));
    call.addDeclaredParameter(new SqlOutParameter("p_exitcode", OracleTypes.NUMBER));
    call.addDeclaredParameter(new SqlOutParameter("p_exittext", OracleTypes.VARCHAR));

    Map<String, Object> results = call.execute(Integer.valueOf(request.getRequestId()), request.getCreator().getEmployeeId(), request.getStatus().toString());
    log.debug(format("exitcode: %s, exittext: %s", results.get("p_exitcode"), results.get("p_exittext")));

    return results;
  }

  /**
   * @param point
   * @param errorMessage
   */
  private void setErrorMessage(Point point, ErrorMessage errorMessage) {
    log.debug("draft point exit: " + errorMessage.toString());

    // Set the error message on the point
    boolean exists = false;

    for (Error error : point.getErrors()) {
      if (error.getProperty().equals(columnNameToProperty(errorMessage.getColumnName()))) {
        exists = true;
        error.getErrors().add(errorMessage.getErrorText());
      }
    }

    if (!exists) {
      point.getErrors().add(new Error(columnNameToProperty(errorMessage.getColumnName()), Collections.singletonList(errorMessage.getErrorText())));
    }
  }

  @Data
  class ErrorMessage {
    final Integer requestId;
    final Integer lineno;
    final String errorText;
    final String columnName;
  }

  /**
   * @param property
   *
   * @return
   */
  private String propertyToColumnName(String property) {
    String columnName = "drp_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property);
    log.trace(format("converted property name %s to column name %s", property, columnName));
    return columnName;
  }

  /**
   * @param columnName
   *
   * @return
   */
  private String columnNameToProperty(String columnName) {
    String property = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName.replace("DRP_", ""));
    log.trace(format("converted column name %s to property name %s", columnName, property));
    return property;
  }
}
