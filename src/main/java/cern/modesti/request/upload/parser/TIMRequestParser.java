/**
 *
 */
package cern.modesti.request.upload.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.model.Person;
import cern.modesti.model.Point;
import cern.modesti.request.upload.exception.RequestParseException;

import com.google.common.base.CaseFormat;

/**
 * @author Justin Lewis Salmon
 *
 */
public class TIMRequestParser extends RequestParser {

  // TODO this is different across different legacy versions
  public static final int FIRST_DATA_COLUMN = 2;
  public static final int LAST_DATA_COLUMN = 77;

  /**
   * @param sheet
   */
  public TIMRequestParser(Sheet sheet) {
    super(sheet);
  }

  @Override
  protected Point parseDataPoint(Row row) {
    Point point = new Point();
    Map<String, Object> properties = new HashMap<>();

    // Use the "line number" as the point id.
    Double id = getNumericCellValue(row.getCell(1));
    if (id != null) {
      point.setId(id.longValue());
    }

    // Loop over all filled cells and add it as a property to the point.
    for (int i = FIRST_DATA_COLUMN; i < LAST_DATA_COLUMN; i++) {
      Cell cell = row.getCell(i);
      Object value = getCellValue(cell);

      if (value != null) {
        properties.put(getColumnTitle(i), value);
      }
    }

    // If the row contained no data, don't return a point object.
    if (properties.isEmpty()) {
      return null;
    }

    // Special case: composite Person object
    Long responsiblePersonId = ((Double) properties.get("responsiblePersonId")).longValue();
    String responsiblePersonName = (String) properties.get("responsiblePersonName");
    properties.put("responsiblePerson", new Person(responsiblePersonId, responsiblePersonName));

    // Special case: composite System/Subsystem
    String system = (String) properties.get("systemName");
    String subsystem = (String) properties.get("subSystemName");
    properties.put("subsystem", system + " " + subsystem);

    // Special case: composite Location object

    point.setProperties(properties);
    return point;
  }

  @Override
  protected String parseDatasource(List<Point> points) {

    // Naive implementation: look at the first point and assume the rest are the same.
    Point point = points.get(0);

    if (point.getProperties().containsKey("tagname")) {
      return "opc";
    } else if (point.getProperties().containsKey("item")) {
      return "dip";
    } else if (point.getProperties().containsKey("blockType")) {
      return "plc";
    } else if (point.getProperties().containsKey("protocol")) {
      return "japc";
    } else if (point.getProperties().containsKey("hostName")) {
      return "diamon";
    } else if (point.getProperties().containsKey("dbTagname")) {
      return "db";
    }

    throw new RequestParseException("Could not determine request datasource");
  }

  /**
   * This function is horrible. Don't read it unless you really have to.
   *
   * @param column
   * @return
   */
  private String getColumnTitle(int column) {
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
    // TODO map this title to the schema field name?
    title = title.replaceAll("[\\(\\)/01]", "").replaceAll("_", " ");
    title = WordUtils.capitalizeFully(title);
    title = title.replaceAll("[\\s]" , "");
    title = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, title);

    // Handle some special cases to avoid duplicate property names
    if (title.equals("id") && column == 8) {
      title = "responsiblePersonId";
    }
    if (title.equals("name") && column == 9) {
      title = "responsiblePersonName";
    }
    if (title.equals("category") && column == 25) {
      title = "alarmCategory";
    }

    return title;
  }

  /**
   * @param cell
   * @return
   */
  private Object getCellValue(Cell cell) {
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
  private Double getNumericCellValue(Cell cell) {
    if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
      return null;
    }
    return cell.getNumericCellValue();
  }
}
