/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.model.SubSystem;
import cern.modesti.request.point.Point;

/**
 * @author Justin Lewis Salmon
 *
 */
public class PVSSRequestParser extends RequestParser {

  /**
   * Minimum supported version of the legacy Excel MODESTI request sheet
   */
  private static final Double MINIMUM_SUPPORTED_VERSION = 1.1;

  public static final int FIRST_DATA_COLUMN = 2;
  public static final int LAST_DATA_COLUMN = 35;
  public static final int POINT_ID_COLUMN = 1;

  /**
   * @param sheet
   */
  public PVSSRequestParser(Sheet sheet) {
    super(sheet);
  }

  @Override
  protected String parseColumnTitle(String title, int column) {
    if (title.equals("id") && column == 8) {
      title = "responsiblePersonId";
    }
    else if (title.equals("name") && column == 9) {
      title = "responsiblePersonName";
    }
    else if (title.equals("value")) {
      title = "alarmValue";
    }
    else if (title.equals("category")) {
      title = "alarmCategory";
    }
    else if (title.equals("number")) {
      title = "buildingNumber";
    }
    else if (title.equals("name")) {
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
  protected int getLastDataColumn() {
    return LAST_DATA_COLUMN;
  }

  @Override
  protected int getPointIdColumn() {
    return POINT_ID_COLUMN;
  }
}
