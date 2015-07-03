package cern.modesti.schema.options;

import cern.modesti.schema.Schema;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.field.OptionsField;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.IntStream;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
public class OptionService {

  private static final Map<String, String> fieldToColumnNames = new HashMap<>();

  /**
   * TODO: the column names from CG_REF_CODES should be standardised to match the field names of the schema. This will remove the need for this explicit
   * mapping.
   */
  static {
    fieldToColumnNames.put("pointDataType", "PTDATATYPES");
    fieldToColumnNames.put("priorityCode", "ALARMPRI");
    fieldToColumnNames.put("deadBandType", "VALDBANDTYPE");
    fieldToColumnNames.put("publisherName", "DIP_PUBLISHER");
    fieldToColumnNames.put("lsacType", "SAFELSAC_ADDTYPE");
    fieldToColumnNames.put("lsacCard", "SAFELSAC_CARDPOS");
    fieldToColumnNames.put("lsacPort", "SAFELSAC_PORTNO");
    fieldToColumnNames.put("lsacRack", "SAFELSAC_RACKNO");
    fieldToColumnNames.put("blockType", "PLC_BLOCKTYPE");
    fieldToColumnNames.put("bitId", "PLC_BITID");
    fieldToColumnNames.put("nativePrefix", "PLC_APIMMD_PREFIX");
    fieldToColumnNames.put("opcdefStatus", "SAFEDEF_STATUS");
    fieldToColumnNames.put("securifireStatus", "SAFESFIRE_SECSTATUS");
    fieldToColumnNames.put("securifireType", "SAFESFIRE_SECTYPE");
    fieldToColumnNames.put("securitonStatus", "SAFESPRO_SECSTATUS");
    fieldToColumnNames.put("securitonMcu", "SAFESPRO_MCU");
    fieldToColumnNames.put("winterChannel", "SAFEWNTR_CHANNEL");
    fieldToColumnNames.put("winterBit", "SAFEWNTR_BITALARM");
    // ??? what is this one
    fieldToColumnNames.put("???", "SAFEPLC_BITID");
  }

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Look at all the "options" fields of the schema that do not explicitly specify a list of options and pre-inject their options from the database. This is
   * to reduce the number of backend calls from the interface.
   *
   * @param schema
   */
  public void injectOptions(Schema schema) {
    for (Category category : schema.getCategories()) {
      for (Field field : category.getFields()) {

        if (field instanceof OptionsField && ((OptionsField) field).getOptions() == null) {
          ((OptionsField) field).setOptions(getOptions(field.getId()));
        }
      }
    }
  }

  /**
   * @param fieldId
   *
   * @return
   */
  private List<String> getOptions(String fieldId) {
    // Translate the field id to its corresponding column name
    String columnName = translateFieldToColumnName(fieldId);

    // Execute the query to get the options
    String optionString = (String) entityManager.createNativeQuery(String.format("SELECT TIMPKUTIL.STF_GET_REFCODE_VALUES('%s') %s FROM DUAL", columnName,
        columnName)).getSingleResult();

    return parseOptionString(optionString);
  }

  /**
   * Take an option string and convert it to a list of options. The option string may be in list format already (e.g. "a, b, c") or it may be in range
   * format (e.g. "0:9").
   *
   * @param optionString
   *
   * @return
   */
  private List<String> parseOptionString(String optionString) {
    if (optionString.contains(",")) {
      return Arrays.asList(optionString.split(","));
    } else if (optionString.contains(":")) {
      Integer start = Integer.valueOf(optionString.substring(0, optionString.indexOf(":")));
      Integer end = Integer.valueOf(optionString.substring(optionString.indexOf(":") + 1, optionString.length()));

      ArrayList<String> options = new ArrayList<>();
      IntStream.rangeClosed(start, end).forEach(n -> options.add(String.valueOf(n)));
      return options;
    }

    return null;
  }

  /**
   * @param fieldId
   *
   * @return
   */
  private String translateFieldToColumnName(String fieldId) {
    return fieldToColumnNames.get(fieldId);
  }
}
