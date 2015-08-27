package cern.modesti.schema.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import cern.modesti.request.point.Point;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.field.Option;
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

  private Map<String, List<Option>> cache = new ConcurrentHashMap<>();

  /**
   * Look at all the "options" fields of the schema that do not explicitly specify a list of options and pre-inject their options from the database. This is
   * to reduce the number of backend calls from the interface.
   *
   * @param schema
   */
  public void injectOptions(Schema schema) {
    for (Category category : schema.getCategories()) {
      category.getFields().forEach(field -> injectOptions(schema, field));
    }

    for (Datasource datasource : schema.getDatasources()) {
      datasource.getFields().forEach(field -> injectOptions(schema, field));
    }
  }

  /**
   *
   * @param schema
   * @param field
   */
  private void injectOptions(Schema schema, Field field) {
    if (field instanceof OptionsField && ((OptionsField) field).getOptions() == null) {

      // The "pointType" field is treated specially. Its options are the possible datasources of the schema.
      if (field.getId().equals("pointType")) {
        // TODO: don't hardcode the use of "name_en" here...
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < schema.getDatasources().size(); i++) {
          options.add(new Option(i, schema.getDatasources().get(i).getName_en()));
        }

        ((OptionsField) field).setOptions(options);
      } else {
        ((OptionsField) field).setOptions(getOptions(field.getId()));
      }
    }
  }

  /**
   * @param property
   *
   * @return
   */
  private List<Option> getOptions(String property) {

    // Translate the field id to its corresponding column name
    String columnName = propertyToColumnName(property);

    if (cache.containsKey(columnName)) {
      return cache.get(columnName);
    }

    // Execute the query to get the options
    Query query = entityManager.createNativeQuery(String.format("SELECT TIMPKUTIL.STF_GET_REFCODE_VALUES('%s') FROM DUAL", columnName));
    String optionString = (String) query.getSingleResult();

    List<Option> options = parseOptionString(optionString);
    if (options != null) {
      options = getOptionMeanings(options, columnName);
    }

    cache.put(columnName, options);
    return options;
  }

  /**
   * Take an option string and convert it to a list of options. The option string may be in list format already (e.g. "a,b,c") or it may be in range
   * format (e.g. "0:9").
   *
   * @param optionString
   *
   * @return
   */
  private List<Option> parseOptionString(String optionString) {
    if (optionString.contains(",")) {
      List<Option> options = new ArrayList<>();
      String[] optionValues = optionString.split(",");

      for (int i = 0; i < optionValues.length; i++) {
        options.add(new Option(i, optionValues[i]));
      }

      return options;
    } else if (optionString.contains(":")) {
      Integer start = Integer.valueOf(optionString.substring(0, optionString.indexOf(":")));
      Integer end = Integer.valueOf(optionString.substring(optionString.indexOf(":") + 1, optionString.length()));

      List<Option> options = new ArrayList<>();
      IntStream.rangeClosed(start, end).forEach(n -> options.add(new Option(n, String.valueOf(n))));
      return options;
    }

    return null;
  }

  /**
   *
   * @param options
   * @param columnName
   * @return
   */
  private List<Option> getOptionMeanings(List<Option> options, String columnName) {
    List<Option> optionsWithMeaning = new ArrayList<>();

    for (Option option : options) {
      Query query = entityManager.createNativeQuery(String.format("SELECT TIMPKUTIL.STF_GET_REFCODES_MEANING('%s', '%s') FROM DUAL", columnName, option.getValue()));
      String meaning = (String) query.getSingleResult();

      if (option.getValue().equals(meaning) || meaning == null || meaning.equals("allowed numeric range") || meaning.equals("allowed range")) {
        optionsWithMeaning.add(option);
      } else {
        optionsWithMeaning.add(new Option(option.getId(), option.getValue() + ": " + meaning));
      }

    }

    return optionsWithMeaning;
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
