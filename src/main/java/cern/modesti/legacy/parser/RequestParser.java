/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.*;

import cern.modesti.model.*;
import cern.modesti.repository.jpa.location.LocationRepository;
import cern.modesti.repository.jpa.person.PersonRepository;
import cern.modesti.repository.jpa.subsystem.SubSystemRepository;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.modesti.legacy.exception.RequestParseException;
import cern.modesti.legacy.exception.VersionNotSupportedException;
import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import cern.modesti.request.point.Point;
import org.springframework.context.ApplicationContext;

/**
 * @author Justin Lewis Salmon
 */
public abstract class RequestParser {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParser.class);

  protected static final int FIRST_DATA_ROW = 7;

  protected Sheet sheet;

  private SubSystemRepository subSystemRepository;
  private PersonRepository personRepository;

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

    Double version = parseVersion();

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
    for (int column = getFirstDataColumn(); column < getLastDataColumn(); column++) {
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
    properties.put("site", parseSite(properties));
    properties.put("zone", parseZone(properties));


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
    Person person = null;
    Long responsiblePersonId = ((Double) properties.get("responsiblePersonId")).longValue();
    String responsiblePersonName = (String) properties.get("responsiblePersonName");

    // Look for the responsible by ID
    List<Person> people = personRepository.findByIdOrName(String.valueOf(responsiblePersonId), String.valueOf(responsiblePersonId));

    // If there are zero or more than 1 person, try to find by name
    if (people.size() == 0 || people.size() > 1) {
      people = personRepository.findByIdOrName(responsiblePersonName, responsiblePersonName);
    }

    if (people.size() == 0 || people.size() > 1) {
      LOG.warn("Could not determine responsible person for point");
    } else {
      person = people.get(0);
      properties.put("responsiblePerson", person);
    }

    return person;
  }

  /**
   * @param properties
   *
   * @return
   */
  protected SubSystem parseSubsystem(Map<String, Object> properties) {
    SubSystem subsystem = null;
    String systemName = (String) properties.get("systemName");
    String subSystemName = (String) properties.get("subSystemName");

    // CSAM requests specify only the subsystem
    if (systemName == null) {
      // Find the subsystem by name (avoiding the "null" string literal)
      List<SubSystem> subsystems = subSystemRepository.findByName(subSystemName);
      if (subsystems.size() == 0 || subsystems.size() > 1) {
        LOG.warn("Could not determine subsystem for point");
      } else {
        subsystem = subsystems.get(0);
      }
    } else {
      subsystem = new SubSystem();
      subsystem.setSystem(systemName);
      subsystem.setSubsystem(subSystemName);
      subsystem.setName(systemName + " " + subSystemName);
    }

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
    String floor = properties.get("floor") != null ? String.valueOf(properties.get("floor")) : null;
    Integer room = properties.get("room") != null ? ((Double) properties.get("room")).intValue() : null;

    location.setLocation(buildingNumber + (floor == null ? "" : ("/" + floor + (room == null ? "" : ("-" + String.format("%03d", room))))));
    //    List<Location> locations = locationRepository.find(query);
    //LOG.debug("found " + locations.size() + " locations with query: " + query);

    //    if (locations.size() == 0) {
    //      //LOG.warn("Could not determine location for point");
    //    } else {
    //      // Otherwise, find the least-specific option, which should be the first in the list
    //      Location location = locations.get(0);
    //      properties.put("location", location);
    //    }

    return location;
  }

  /**
   *
   * @param properties
   * @return
   */
  private Site parseSite(Map<String, Object> properties) {
    Site site = new Site();
    site.setName((String) properties.get("site"));
    return site;
  }

  /**
   *
   * @param properties
   * @return
   */
  private Zone parseZone(Map<String, Object> properties) {
    Zone zone = new Zone();
    zone.setName(properties.get("zone") != null ? String.valueOf(((Double) properties.get("zone")).intValue()) : "");
    return zone;
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
   * @return
   */
  protected abstract int getLastDataColumn();

  /**
   *
   * @return
   */
  protected abstract int getPointIdColumn();

  /**
   *
   * @param context
   */
  public void setApplicationContext(ApplicationContext context) {
    this.subSystemRepository = context.getBean(SubSystemRepository.class);
    this.personRepository = context.getBean(PersonRepository.class);
  }
}
