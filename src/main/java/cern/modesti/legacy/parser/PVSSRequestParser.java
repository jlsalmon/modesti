/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.request.point.Point;

/**
 * @author Justin Lewis Salmon
 */
public class PVSSRequestParser extends RequestParser {

  /**
   * Minimum supported version of the legacy Excel MODESTI request sheet
   */
  private static final Double MINIMUM_SUPPORTED_VERSION = 1.1;

  public static final int FIRST_DATA_COLUMN = 2;
  public static final int LAST_DATA_COLUMN = 36;
  public static final int POINT_ID_COLUMN = 1;

  private Map<String, String> columnTitleMappings = new HashMap<>();

  /**
   * @param sheet
   */
  public PVSSRequestParser(Sheet sheet) {
    super(sheet);

    // General mappings
    columnTitleMappings.put("pointDataType", "pointDatatype");
    columnTitleMappings.put("otherCode", "otherEquipCode");
    columnTitleMappings.put("id", "responsiblePersonId");
    columnTitleMappings.put("pointComplementaryInfo", "pointCompInfo");

    // Alarm mappings
    columnTitleMappings.put("pvssAlarmSource", "laserSource");
    columnTitleMappings.put("value", "alarmValue");
    columnTitleMappings.put("category", "alarmCategory");
    columnTitleMappings.put("autoCallNumber", "autocallNumber");

    // Alarm Help mappings
    columnTitleMappings.put("alarmConsequences", "alarmConseq");
    columnTitleMappings.put("taskDuringWorkingHoursActionHo", "workHoursTask");
    columnTitleMappings.put("taskOutsideWorkingHoursActionHho", "outsideHoursTask");

    // Location mappings
    columnTitleMappings.put("site", "functionalityCode");
    columnTitleMappings.put("number", "buildingNumber");
    columnTitleMappings.put("floor", "buildingFloor");
    columnTitleMappings.put("room", "buildingRoom");

    // Analogue mappings
    columnTitleMappings.put("valueDeadBand", "valueDeadband");
    columnTitleMappings.put("deadBandType", "deadbandType");
  }

  @Override
  protected String parseColumnTitle(String title, int column) {
    String mapping = columnTitleMappings.get(title);
    if (mapping != null) {
      title = mapping;
    } else if (title.equals("name") && column == 9) {
      title = "responsiblePersonName";
    } else if (title.equals("name") && column == 15) {
      title = "buildingName";
    }

    return title;
  }

  @Override
  protected List<String> parseCategories(List<Point> points) {
    // PVSS requests don't really have categories
    return Collections.singletonList("PVSS");
  }

  @Override
  protected Double getMinimumSupportedVersion() {
    return MINIMUM_SUPPORTED_VERSION;
  }

  @Override
  protected int getFirstDataColumn() {
    return FIRST_DATA_COLUMN;
  }

  @Override
  protected int getLastDataColumn(Double version) {
    return LAST_DATA_COLUMN;
  }

  @Override
  protected int getPointIdColumn() {
    return POINT_ID_COLUMN;
  }
}
