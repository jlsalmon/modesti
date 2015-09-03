/**
 *
 */
package cern.modesti.upload.parser;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

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
  public static final int POINT_ID_COLUMN = 2;

  private Map<String, String> columnTitleMappings = new HashMap<>();

  /**
   * @param sheet
   */
  public CSAMRequestParser(Sheet sheet) {
    super(sheet);

    // General mappings
    columnTitleMappings.put("description", "pointDescription");
    columnTitleMappings.put("dataType", "pointDatatype");
    columnTitleMappings.put("equipementCse", "csamCsename");
    columnTitleMappings.put("equipementCapteur", "csamDetector");
    columnTitleMappings.put("typeDetectionSubSystem", "subSystemName");
    columnTitleMappings.put("identifiant", "responsiblePersonId");
    columnTitleMappings.put("nom", "responsiblePersonName");
    columnTitleMappings.put("attribut", "pointAttribute");

    // Alarm mappings
    columnTitleMappings.put("etatActif", "alarmValue");
    columnTitleMappings.put("niveauAlarme", "priorityCode");
    columnTitleMappings.put("aideAlarmeNomDeFichierOuTexte", "aideAlarme");

    // Location mappings
    columnTitleMappings.put("site", "functionalityCode");
    columnTitleMappings.put("zone", "safetyZone");
    columnTitleMappings.put("numero", "buildingNumber");
    columnTitleMappings.put("sigle", "buildingName");
    columnTitleMappings.put("etage", "buildingFloor");
    columnTitleMappings.put("piece", "buildingRoom");

    // Monitoring mappings
    columnTitleMappings.put("equipementSurveillance", "csamPlcname");
    columnTitleMappings.put("cablage", "cabling");

    // Analogue mappings
    columnTitleMappings.put("min", "lowLimit");
    columnTitleMappings.put("max", "highLimit");
    columnTitleMappings.put("zoneMorte", "valueDeadband");
    columnTitleMappings.put("unite", "units");

    // Logging mappings
    columnTitleMappings.put("valeurZoneMorte", "logValueDeadband");
    columnTitleMappings.put("zoneMorteTemps", "logTimeDeadband");

    // Alarm Help mappings
    columnTitleMappings.put("actionHeuresOuvrables", "workHoursTask");
    columnTitleMappings.put("actionHorsHeuresOuvrables", "outsideHoursTask");
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
      title = "plcBlockType";
    } else if (title.equals("word") && column == 27) {
      title = "plcWordId";
    } else if (title.equals("bit") && column == 28) {
      title = "plcBitId";
    } else if (title.equals("nativePrefix")) {
      title = "plcNativePrefix";
    } else if (title.equals("slaveAddress")) {
      title = "plcSlaveAddress";
    } else if (title.equals("connectId")) {
      title = "plcConnectId";
    }

    // PLC - OPC special cases
    if (title.equals("byte") && column == 32) {
      title = "safeplcByteId";
    } else if (title.equals("bit") && column == 33) {
      title = "safeplcBitId";
    }

    // WINTER special cases
    if (title.equals("voie")) {
      title = "winterChannel";
    } else if (title.equals("bit") && column == 35) {
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
    if (title.equals("module")) {
      title = "safedefModule";
    } else if (title.equals("line")) {
      title = "safedefLine";
    } else if (title.equals("address")) {
      title = "safedefAddress";
    } else if (title.equals("status") && (column == 47 || column == 48)) {
      title = "safedefStatus";
    }

    return title;
  }

  @Override
  protected String parsePointType(Map<String, Object> properties) {
    if (properties.containsKey("lsacRack")) {
      return "LSAC";
    } else if (properties.containsKey("plcBlockType")) {
      return "APIMMD";
    } else if (properties.containsKey("safeplcByteId")) {
      return "SFPLC";
    } else if (properties.containsKey("winterStatus")) {
      return "WINTER";
    } else if (properties.containsKey("securitonGroup")) {
      return "SECURITON";
    } else if (properties.containsKey("securifireGroup")) {
      return "SECURIFIRE";
    } else if (properties.containsKey("safedefModule")) {
      return "DEF";
    }

    return null;
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
    if (version == 5.2) {
      return 56;
    } else {
      return 57;
    }
  }

  @Override
  protected int getPointIdColumn() {
    return POINT_ID_COLUMN;
  }
}