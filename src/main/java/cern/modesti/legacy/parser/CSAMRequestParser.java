/**
 *
 */
package cern.modesti.legacy.parser;

import cern.modesti.legacy.exception.RequestParseException;
import cern.modesti.request.point.Point;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 *
 */
public class CSAMRequestParser extends RequestParser {

  /**
   * Minimum supported version of the legacy Excel MODESTI request sheet
   */
  private static final Double MINIMUM_SUPPORTED_VERSION = 5.2;

  public static final int FIRST_DATA_COLUMN = 3;
  public static final int LAST_DATA_COLUMN = 56;
  public static final int POINT_ID_COLUMN = 2;

  private Map<String, String> columnTitleMappings = new HashMap<>();

  /**
   * @param sheet
   */
  public CSAMRequestParser(Sheet sheet) {
    super(sheet);

    // General mappings
    columnTitleMappings.put("description", "pointDescription");
    columnTitleMappings.put("dataType", "pointDataType");
    columnTitleMappings.put("equipementCse", "gmaoCode");
    columnTitleMappings.put("equipementCapteur", "otherCode");
    columnTitleMappings.put("typeDetectionSubSystem", "subSystemName");
    columnTitleMappings.put("identifiant", "responsiblePersonId");
    columnTitleMappings.put("nom", "responsiblePersonName");
    columnTitleMappings.put("attribut", "pointAttribute");

    // Alarm mappings
    columnTitleMappings.put("etatActif", "alarmValue");
    columnTitleMappings.put("niveauAlarme", "priorityCode");

    // Location mappings
    columnTitleMappings.put("numero", "buildingNumber");
    columnTitleMappings.put("sigle", "buildingName");
    columnTitleMappings.put("etage", "floor");
    columnTitleMappings.put("piece", "room");

    // Monitoring mappings
    columnTitleMappings.put("equipementSurveillance", "monitoringEquipmentName");

    // Analogue mappings
    columnTitleMappings.put("min", "lowLimit");
    columnTitleMappings.put("max", "highLimit");
    columnTitleMappings.put("zoneMorte", "valueDeadBand");
    columnTitleMappings.put("unite", "units");

    // Logging mappings
    columnTitleMappings.put("valeurZoneMorte", "valueDeadBand");
    columnTitleMappings.put("zoneMorteTemps", "timeDeadBand");

    // Alarm Help mappings
    columnTitleMappings.put("actionHeuresOuvrables", "taskDuringWorkingHoursActionHo");
    columnTitleMappings.put("actionHorsHeuresOuvrables", "taskDuringWorkingHoursActionHho");
  }

  @Override
  protected String parseColumnTitle(String title, int column) {
    // Fix the French column titles back to English to match the schema
    String mapping = columnTitleMappings.get(title);
    if (mapping != null) {
      title = mapping;
    }

    // LSAC special cases
    if (title.equals("type") && column == 22) {
      title = "lsacType";
    } else if (title.equals("rack")) {
      title = "lsacRack";
    } else if (title.equals("card")) {
      title = "lsacCard";
    } else if (title.equals("port")) {
      title = "lsacPort";
    }

    // PLC - APIMMD special cases
    if (title.equals("block") && column == 26) {
      title = "blockType";
    } else if (title.equals("word") && column == 27) {
      title = "wordId";
    } else if (title.equals("bit") && column == 28) {
      title = "bitId";
    }

    // PLC - OPC special cases
    if (title.equals("byte") && column == 32) {
      title = "opcByte";
    } else if (title.equals("bit") && column == 33) {
      title = "opcBit";
    }

    // WINTER special cases
    if (title.equals("voie")) {
      title = "winterChannel";
    } else
    if (title.equals("bit") && column == 35) {
      title = "winterBit";
    }

    // SECURITON special cases
    if (title.equals("group") && column == 37) {
      title = "securitonGroup";
    } else if (title.equals("area")) {
      title = "securitonArea";
    } else if (title.equals("detecteur") && column == 38) {
      title = "securitonDetecteur";
    } else if (title.equals("status") && column == 39) {
      title = "securitonStatus";
    } else if (title.equals("mcu")) {
      title = "securitonMcu";
    }

    // SECURIFIRE special cases
    if (title.equals("group") && column == 41) {
      title = "securifireGroup";
    } else if (title.equals("detecteur") && column == 42) {
      title = "securifireDetecteur";
    } else if (title.equals("status") && column == 43) {
      title = "securifireStatus";
    } else if (title.equals("type") && column == 44) {
      title = "securifireType";
    }

    // OPCDEF special cases
    if (title.equals("status") && column == 48) {
      title = "opcdefStatus";
    }

    return title;
  }

  @Override
  protected List<String> parseCategories(List<Point> points) {
    List<String> categories = new ArrayList<>();

    for (Point point : points) {
      if (point.getProperties().containsKey("lsacRack")) {
        if (!categories.contains("LSAC")) categories.add("LSAC");
      } else if (point.getProperties().containsKey("blockType")) {
        if (!categories.contains("APIMMD")) categories.add("APIMMD");
      } else if (point.getProperties().containsKey("opcByte")) {
        if (!categories.contains("OPC")) categories.add("OPC");
      } else if (point.getProperties().containsKey("winterStatus")) {
        if (!categories.contains("WINTER")) categories.add("WINTER");
      } else if (point.getProperties().containsKey("securitonGroup")) {
        if (!categories.contains("SECIRITON")) categories.add("SECIRITON");
      } else if (point.getProperties().containsKey("securifireGroup")) {
        if (!categories.contains("SECURIFIRE")) categories.add("SECURIFIRE");
      } else if (point.getProperties().containsKey("module")) {
        if (!categories.contains("OPCDEF")) categories.add("OPCDEF");
      }
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
