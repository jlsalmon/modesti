package cern.modesti.schema.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;

import cern.modesti.schema.Schema;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.field.OptionsField;

import com.google.common.base.CaseFormat;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
public class OptionService {

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
   * @param property
   *
   * @return
   */
  private List<String> getOptions(String property) {
    // Translate the field id to its corresponding column name
    String columnName = propertyToColumnName(property);

    // Execute the query to get the options
    Query query = entityManager.createNativeQuery(String.format("SELECT TIMPKUTIL.STF_GET_REFCODE_VALUES('%s') %s FROM DUAL", columnName, columnName));
    String optionString = (String) query.getSingleResult();

    return parseOptionString(optionString);
  }

  /**
   * Take an option string and convert it to a list of options. The option string may be in list format already (e.g. "a,b,c") or it may be in range
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
  private String propertyToColumnName(String fieldId) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldId);
  }
}
