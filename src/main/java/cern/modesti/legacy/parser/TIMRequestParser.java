/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.legacy.exception.RequestParseException;
import cern.modesti.model.SubSystem;
import cern.modesti.request.point.Point;

/**
 * @author Justin Lewis Salmon
 *
 */
public class TIMRequestParser extends RequestParser {

  /**
   * Minimum supported version of the legacy Excel MODESTI request sheet
   */
  private static final Double MINIMUM_SUPPORTED_VERSION = 4.0;

  public static final int FIRST_DATA_COLUMN = 2;
  public static final int LAST_DATA_COLUMN = 77;
  public static final int POINT_ID_COLUMN = 1;


  /**
   * @param sheet
   */
  public TIMRequestParser(Sheet sheet) {
    super(sheet);
  }

  @Override
  protected String parseColumnTitle(String title, int column) {
    // Handle some special cases to avoid duplicate property names
    if (title.equals("id") && column == 8) {
      title = "responsiblePersonId";
    }
    else if (title.equals("name") && column == 9) {
      title = "responsiblePersonName";
    }
    else if (title.equals("category") && column == 25) {
      title = "alarmCategory";
    }
    if (title.equals("value")) {
      title = "alarmValue";
    }
    if (title.equals("number")) {
      title = "buildingNumber";
    }
    if (title.equals("name")) {
      title = "buildingName";
    }

    return title;
  }

  @Override
  protected List<String> parseCategories(List<Point> points) {
    List<String> categories = new ArrayList<>();

    // Naive implementation: look at the first point and assume the rest are the same.
    Point point = points.get(0);

    if (point.getProperties().containsKey("tagname")) {
      categories.add("OPC");
    } else if (point.getProperties().containsKey("item")) {
      categories.add("DIP");
    } else if (point.getProperties().containsKey("blockType")) {
      categories.add("PLC");
    } else if (point.getProperties().containsKey("protocol")) {
      categories.add("JAPC");
    } else if (point.getProperties().containsKey("hostName")) {
      categories.add("DIAMON");
    } else if (point.getProperties().containsKey("dbTagname")) {
      categories.add("DB");
    }

    if (categories.isEmpty()) {
      throw new RequestParseException("Could not determine request categories");
    }

    return categories;
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
