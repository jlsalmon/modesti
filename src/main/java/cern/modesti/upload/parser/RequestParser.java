/**
 *
 */
package cern.modesti.upload.parser;

import cern.modesti.upload.exception.RequestParseException;
import cern.modesti.upload.exception.VersionNotSupportedException;
import cern.modesti.repository.jpa.alarm.AlarmCategory;
import cern.modesti.repository.jpa.equipment.MonitoringEquipment;
import cern.modesti.repository.jpa.equipment.MonitoringEquipmentRepository;
import cern.modesti.repository.jpa.gmao.GmaoCode;
import cern.modesti.repository.jpa.location.BuildingName;
import cern.modesti.repository.jpa.location.Location;
import cern.modesti.repository.jpa.location.functionality.Functionality;
import cern.modesti.repository.jpa.location.functionality.FunctionalityRepository;
import cern.modesti.repository.jpa.location.zone.Zone;
import cern.modesti.repository.jpa.person.Person;
import cern.modesti.repository.jpa.person.PersonRepository;
import cern.modesti.repository.jpa.subsystem.SubSystem;
import cern.modesti.repository.jpa.subsystem.SubSystemRepository;
import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import cern.modesti.request.point.Point;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public abstract class RequestParser {

  protected static final int FIRST_DATA_ROW = 7;

  protected Sheet sheet;
  private Double version;

  private SubSystemRepository subSystemRepository;
  private Map<String, SubSystem> subSystemCache = new HashMap<>();
  private PersonRepository personRepository;
  private Map<String, Person> personCache = new HashMap<>();
  private FunctionalityRepository functionalityRepository;
  private Map<String, Functionality> functionalityCache = new HashMap<>();
  private MonitoringEquipmentRepository monitoringEquipmentRepository;
  private Map<String, MonitoringEquipment> monitoringEquipmentCache = new HashMap<>();

  /**
   * @param sheet
   */
  public RequestParser(Sheet sheet) {
    this.sheet = sheet;
  }

  /**
   *
   * @return
   */
  public Request parseRequest() {
    Request request = new Request();

    request.setDomain(parseDomain());
    request.setType(parseRequestType());

    version = parseVersion();

    // Parse all the points from the request
    List<Point> points = parseDataPoints();
    if (points.isEmpty()) {
      throw new RequestParseException("Request contained no data points");
    }
    request.setPoints(points);

    // Figure out the data sources
    request.setCategories(parseCategories(points));

    // Figure out the subsystem: assume first
    request.setSubsystem((SubSystem) points.get(0).getProperties().get("subsystem"));

    return request;
  }

  /**
   *
   * @return
   */
  private String parseDomain() {
    return sheet.getRow(0).getCell(0).getStringCellValue().trim();
  }

  /**
   * Only versions greater than 4.2 are supported.
   *
   * @return
   */
  private Double parseVersion() {
    Double version = Double.valueOf(sheet.getRow(0).getCell(3).getStringCellValue());
    if (version < getMinimumSupportedVersion()) {
      throw new VersionNotSupportedException("Legacy MODESTI Excel file version " + version + " not supported. Minimum supported version is " + getMinimumSupportedVersion());
    }
    return version;
  }

  /**
   *
   * @return
   */
  private RequestType parseRequestType() {
    String type = sheet.getRow(0).getCell(1).getStringCellValue();

    if (type.contains("creation")) {
      return RequestType.CREATE;
    } else if (type.contains("modification")) {
      return RequestType.MODIFY;
    } else if (type.contains("deletion")) {
      return RequestType.DELETE;
    } else {
      throw new RequestParseException("Invalid request type: " + type);
    }
  }

  /**
   *
   * @return
   */
  private List<Point> parseDataPoints() {
    List<Point> points = new ArrayList<Point>();

    for (int i = FIRST_DATA_ROW; i < sheet.getLastRowNum(); i++) {
      Point point = parseDataPoint(sheet.getRow(i));

      // If we find an empty row, stop processing.
      if (point == null) {
        break;
      }

      points.add(point);
    }

    return points;
  }

  /**
   * @param row
   *
   * @return null if the row contained no data
   */
  protected Point parseDataPoint(Row row) {
    Point point = new Point();
    Map<String, Object> properties = new HashMap<>();

    // Use the "line number" as the point id.
    Double id = getNumericCellValue(row.getCell(getPointIdColumn()));
    if (id != null) {
      point.setId(id.longValue());
    }

    // Loop over all filled cells and add it as a property to the point.
    for (int column = getFirstDataColumn(); column < getLastDataColumn(version); column++) {
      Cell cell = row.getCell(column);
      Object value = getCellValue(cell);

      if (value != null) {
        String title = getColumnTitle(column);

        // Handle domain-specific special cases
        title = parseColumnTitle(title, column);

        properties.put(title, value);
      }
    }

    // If the row contained no data, don't return a point object.
    if (properties.isEmpty()) {
      return null;
    }


    properties.put("responsiblePerson", parseResponsiblePerson(properties));
    properties.put("subsystem", parseSubsystem(properties));
    properties.put("location", parseLocation(properties));
    properties.put("buildingName", new BuildingName((String) properties.get("buildingName")));
    properties.put("gmaoCode", new GmaoCode((String) properties.get("gmaoCode")));
    properties.put("csamDetector", new GmaoCode((String) properties.get("csamDetector")));
    properties.put("csamCsename", new GmaoCode((String) properties.get("csamCsename")));
    properties.put("functionality", parseFunctionality(properties));
    properties.put("alarmCategory", new AlarmCategory((String) properties.get("alarmCategory")));
    if (properties.get("safetyZone") != null) {
      properties.put("safetyZone", new Zone(String.valueOf(((Double) properties.get("safetyZone")).intValue())));
    }
    if (properties.get("monitoringEquipmentName") != null) {
      properties.put("monitoringEquipment", parseMonitoringEquipment(properties, "monitoringEquipmentName"));
    }
//    if (properties.get("csamPlcname") != null) {
//      properties.put("csamPlcname", parseMonitoringEquipment(properties, "csamPlcname"));
//    }
    properties.remove("aideAlarme");


    point.setProperties(properties);
    return point;
  }

  /**
   *
   * @param title
   * @param column
   * @return
   */
  protected abstract String parseColumnTitle(String title, int column);

  /**
   * @param points
   *
   * @return
   */
  protected abstract List<String> parseCategories(List<Point> points);

  /**
   *
   * @param properties
   * @return
   */
  protected Person parseResponsiblePerson(Map<String, Object> properties) {
    Long responsiblePersonId = ((Double) properties.get("responsiblePersonId")).longValue();
    String responsiblePersonName = (String) properties.get("responsiblePersonName");

    Person person = personCache.get(String.valueOf(responsiblePersonId));

    if (person == null) {
      // Look for the responsible by ID
      List<Person> people = personRepository.findByIdOrName(String.valueOf(responsiblePersonId), String.valueOf(responsiblePersonId));

      // If there are zero or more than 1 person, try to find by name
      if (people.size() == 0 || people.size() > 1) {
        people = personRepository.findByIdOrName(responsiblePersonName, responsiblePersonName);
      }

      if (people.size() == 0 || people.size() > 1) {
        log.warn("Could not determine responsible person for point");
        person = new Person();
      } else {
        person = people.get(0);
        properties.put("responsiblePerson", person);
      }
    }

    personCache.put(String.valueOf(responsiblePersonId), person);
    properties.remove("responsiblePersonId");
    properties.remove("responsiblePersonName");
    return person;
  }

  /**
   * @param properties
   *
   * @return
   */
  protected SubSystem parseSubsystem(Map<String, Object> properties) {
    String systemName = (String) properties.get("systemName");
    String subSystemName = (String) properties.get("subSystemName");

    if (systemName == null) {
      // CSAM requests specify only the subsystem because they are all SECU systems
      systemName = "SECU";
    }

    SubSystem subsystem = subSystemCache.get(systemName + " " + subSystemName);

    if (subsystem == null) {
      subsystem = new SubSystem();
      subsystem.setSystem(systemName);
      subsystem.setSubsystem(subSystemName);
      subsystem.setValue(systemName + " " + subSystemName);
    }

    subSystemCache.put(subsystem.getValue(), subsystem);
    properties.remove("systemName");
    properties.remove("subSystemName");
    return subsystem;
  }

  /**
   *
   * @param properties
   * @return
   */
  private Location parseLocation(Map<String, Object> properties) {
    Location location = new Location();

    String buildingNumber = String.valueOf(((Double) properties.get("buildingNumber")).intValue());
    location.setBuildingNumber(buildingNumber);

    if (properties.get("floor") != null) {
      location.setFloor(String.valueOf(properties.get("floor")));
    }

    if (properties.get("room") != null) {
      String room = String.format("%03d", ((Double) properties.get("room")).intValue());
    }

    String value = buildingNumber + (location.getFloor() == null ? "" : "/" + location.getFloor());
    value += location.getRoom() == null ? "" : ("-" +  location.getRoom());
    location.setValue(value);

    properties.remove("buildingNumber");
    properties.remove("floor");
    properties.remove("room");
    return location;
  }

  /**
   *
   * @param properties
   * @return
   */
  private Functionality parseFunctionality(Map<String, Object> properties) {
    String functionalityCode = (String) properties.get("functionalityCode");
    Functionality functionality = functionalityCache.get(functionalityCode);

    if (functionality == null) {
      functionality = functionalityRepository.findOne(functionalityCode);
    }

    if (functionality == null) {
      log.warn("Could not determine functionality for point");
      functionality = new Functionality();
    }

    functionalityCache.put(functionalityCode, functionality);
    properties.remove("functionalityCode");
    return functionality;
  }

  /**
   *
   * @param properties
   * @return
   */
  private MonitoringEquipment parseMonitoringEquipment(Map<String, Object> properties, String property) {
    String monitoringEquipmentName = (String) properties.get(property);
    MonitoringEquipment monitoringEquipment = monitoringEquipmentCache.get(monitoringEquipmentName);

    if (monitoringEquipment == null) {
      monitoringEquipment = monitoringEquipmentRepository.findOneByValue(monitoringEquipmentName);

      if (monitoringEquipment == null) {
        // Try to find by the "impname" property
        monitoringEquipment = monitoringEquipmentRepository.findOneByName(monitoringEquipmentName);

        if (monitoringEquipment == null) {
          log.warn("Could not determine monitoring equipment for point");
          monitoringEquipment = new MonitoringEquipment();
        }
      }
    }

    monitoringEquipmentCache.put(monitoringEquipmentName, monitoringEquipment);
    properties.remove("monitoringEquipmentName");
    return monitoringEquipment;
  }

  /**
   * This function is horrible. Don't read it unless you really have to.
   *
   * @param column
   * @return
   */
  protected String getColumnTitle(int column) {
    // The title could be in 1 of 3 rows.
    Row titleRow1 = sheet.getRow(3);
    Row titleRow2 = sheet.getRow(4);
    Row titleRow3 = sheet.getRow(5);

    List<Row> titleRows = new ArrayList<>(Arrays.asList(titleRow3, titleRow2, titleRow1));

    // Try to find the title in each row, starting with the last.
    String title = null;
    for (Row titleRow : titleRows) {
      title = titleRow.getCell(column).getStringCellValue();
      if (title != null && !title.isEmpty()) {
        break;
      }
    }

    // Remove slashes, underscores, numbers and parentheses and convert to camel case
    title = title.replaceAll("[\\(\\)/01]", "").replaceAll("_", " ");
    title = WordUtils.capitalizeFully(title);
    title = title.replaceAll("[\\s]" , "");
    title = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, title);

    return title;
  }

  /**
   * @param cell
   * @return
   */
  protected Object getCellValue(Cell cell) {
    String stringValue = getStringCellValue(cell);
    if (stringValue != null && !stringValue.isEmpty()) {
      return stringValue;
    }

    Double numericValue = getNumericCellValue(cell);
    if (numericValue != null) {
      return numericValue;
    }

    return null;
  }

  /**
   *
   * @param cell
   * @return
   */
  private String getStringCellValue(Cell cell) {
    if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
      return null;
    }
    return cell.getStringCellValue();
  }

  /**
   *
   * @param cell
   * @return
   */
  protected Double getNumericCellValue(Cell cell) {
    if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
      return null;
    }
    return cell.getNumericCellValue();
  }

  /**
   *
   * @return
   */
  protected abstract Double getMinimumSupportedVersion();

  /**
   *
   * @return
   */
  protected abstract int getFirstDataColumn();

  /**
   *
   * @param version
   * @return
   */
  protected abstract int getLastDataColumn(Double version);

  /**
   *
   * @return
   */
  protected abstract int getPointIdColumn();

  /**
   *
   * @param personRepository
   */
  public void setPersonRepository(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }

  /**
   *
   * @param subSystemRepository
   */
  public void setSubSystemRepository(SubSystemRepository subSystemRepository) {
    this.subSystemRepository = subSystemRepository;
  }

  /**
   *
   * @param functionalityRepository
   */
  public void setFunctionalityRepository(FunctionalityRepository functionalityRepository) {
    this.functionalityRepository = functionalityRepository;
  }

  /**
   *
   * @param monitoringEquipmentRepository
   */
  public void setMonitoringEquipmentRepository(MonitoringEquipmentRepository monitoringEquipmentRepository) {
    this.monitoringEquipmentRepository = monitoringEquipmentRepository;
  }
}
