package cern.modesti.workflow.validation;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import cern.modesti.request.point.Point;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Constraint;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.field.Option;
import cern.modesti.schema.field.OptionsField;
import cern.modesti.util.PointUtils;
import cern.modesti.util.SchemaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class CoreValidationService {

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private RequestService requestService;

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  private ObjectMapper mapper = new ObjectMapper();


  public void validateRequest(Request request) {
    boolean valid = true;
    Schema schema = schemaRepository.findOne(request.getDomain());

    // Reset all points and clear any error messages.
    for (Point point : request.getPoints()) {
      point.setValid(null);
      point.setErrors(new ArrayList<>());
    }

    // Concatenate all categories and datasources
    List<Category> categories = new ArrayList<>(schema.getCategories());
    categories.addAll(schema.getDatasources());

    // Validate the mutually exclusive column group specifications.
    if (!validateMutualExclusions(request, categories)) {
      valid = false;
    }

    // Validate the constraints of the schema. This checks things like unique
    // column groups and mutually inclusive fields.
    if (!validateConstraints(request, categories)) {
      valid = false;
    }

    // Validate each point separately. This checks things like required
    // values, min/max length, valid values etc.
    if (!validatePoints(request, categories)) {
      valid = false;
    }

    request.setValid(valid);
    requestService.save(request);

    if (!valid) {
      log.info(format("request #%s failed validation, not invoking custom validator", request.getRequestId()));
      return;
    }

    log.info(format("request #%s is valid, invoking custom validator", request.getRequestId()));

    RequestValidator validator = requestProviderRegistry.getPluginFor(request).getRequestValidator();
    if (validator == null) {
      log.info(format("custom validator not provided for request #%s", request.getRequestId()));
      return;
    }

    valid = validator.validateRequest(request, schema);

    request.setValid(valid);
    requestService.save(request);
  }


  private boolean validatePoints(Request request, List<Category> categories) {
    boolean valid = true;

    for (Point point : request.getPoints()) {

      // Ignore empty points
      if (PointUtils.isEmptyPoint(point)) {
        continue;
      }

      for (Category category : categories) {
        for (Field field : category.getFields()) {

          String propertyName = SchemaUtils.getPropertyName(field);
          Object value = PointUtils.getValueByPropertyName(point, propertyName);

          // Check for invalid fields
          if (!isValidValue(value, point, field)) {
            point.setValid(false);
            valid = false;
            // TODO: set category to invalid
            // point.properties.valid = category.valid = valid = false;
            point.addErrorMessage(category.getName(), propertyName, "Value '" + value + "' is not a legal option for field '" + field.getName()
                + "'. Please select a value from the list.");
          }

          // Validate unique fields
          if (field.getUnique() != null) {
            Constraint constraint = new Constraint("unique", Collections.singletonList(field.getId()), null);

            if (!Constraints.validate(constraint, request, category)) {
              valid = false;
            }
          }

          // Required fields (can be simple boolean or condition list)
          boolean required = false;
          if (field.getRequired() instanceof Boolean && (Boolean) field.getRequired()) {
            required = true;
          } else if (field.getRequired() != null) {
            required = Conditionals.evaluate(field.getRequired(), point, request.getStatus());
          }

          if (required) {
            if (value == "" || value == null) {
              point.setValid(false);
              valid = false;
              // TODO: set category to invalid
              // point.properties.valid = category.valid = valid = false;
              point.addErrorMessage(category.getName(), propertyName, "Field '" + field.getName() + "' is mandatory");
            }
          }

          // Min length
          if (field.getMinLength() != null) {
            if (value != null && value.toString().length() < field.getMinLength()) {
              point.setValid(false);
              valid = false;
              // TODO: set category to invalid
              // point.properties.valid = category.valid = valid = false;
              point.addErrorMessage(category.getName(), propertyName,
                  "Field '" + field.getName() + "' must be at least " + field.getMinLength() + " characters in length");
            }
          }

          // Max length
          if (field.getMaxLength() != null) {
            if (value != null && value.toString().length() > field.getMaxLength()) {
              point.setValid(false);
              valid = false;
              // TODO: set category to invalid
              // point.properties.valid = category.valid = valid = false;
              point.addErrorMessage(category.getName(), propertyName,
                  "Field '" + field.getName() + "' must not exceed " + field.getMinLength() + " characters in length");
            }
          }

          // Numeric fields
          if (field.getType().equals("numeric")) {
            if (value != null && !NumberUtils.isNumber(value.toString())) {
              point.setValid(false);
              valid = false;
              // TODO: set category to invalid
              // point.properties.valid = category.valid = valid = false;
              point.addErrorMessage(category.getName(), propertyName, "Value for '" + field.getName() + "' must be numeric");
            }
          }
        }
      }
    }

    return valid;
  }

  private boolean validateMutualExclusions(Request request, List<Category> categories) {
    boolean valid = true;

    for (Category category : categories) {
      if (category.getExcludes() == null) {
        continue;
      }

      for (String exclude : category.getExcludes()) {

        // Get the excluded category
        for (Category cat : categories) {
          if (cat.getId().equals(exclude)) {
            Category excludedCategory = cat;

            // For each point, check that if one or more of the fields of this category and one or more of the fields of the excluded category are filled. If
            // so, say something like "Fields in the "Alarms" group cannot be used if fields in the "Commands" group have been specified.".
            for (Point point : request.getPoints()) {
              List<Field> emptyFields = SchemaUtils.getEmptyFields(point, category.getFields());

              // If at least one of the fields of this category are filled, then we must check the excluded category.
              if (emptyFields.size() != category.getFields().size()) {
                emptyFields = SchemaUtils.getEmptyFields(point, excludedCategory.getFields());

                if (emptyFields.size() != excludedCategory.getFields().size()) {
                  point.setValid(false);
                  valid = false;
                  // TODO: set category to invalid
                  // point.properties.valid = category.valid = valid = false;
                  point.addErrorMessage(category.getName(), "", "Fields in the '" + category.getName() + "' group cannot be used if fields in the '"
                      + excludedCategory.getName() + "' group have been specified.");
                }
              }
            }
          }
        }
      }
    }

    return valid;
  }

  private boolean validateConstraints(Request request, List<Category> categories) {
    boolean valid = true;

    for (Category category : categories) {
      if (category.getConstraints() == null) {
        continue;
      }

      for (Constraint constraint : category.getConstraints()) {
        if (!Constraints.validate(constraint, request, category)) {
          valid = false;
        }
      }
    }

    return valid;
  }

  private boolean isValidValue(Object value, Point point, Field field) {
    // If the value is empty, it's technically not invalid.
    if (value ==  null || value == "") {
      return true;
    }

    // If we have an options field, check that the value is in the list of options
    if (field.getType().equals("options"))  {
      OptionsField optionsField = (OptionsField) field;
      List<Object> options = (List<Object>) optionsField.getOptions();

      if (options != null) {
        for (Object option : options) {
          option = option instanceof Map ? mapper.convertValue(option, Option.class).getValue() : option;

          if ((option instanceof Number) && (NumberUtils.isNumber(value.toString())) && option.toString().equals(value)) {
            return true;
          } else if (option.equals(value)) {
            return true;
          }
        }
      }

      return false;
    }

    // Otherwise, if we have an autocomplete field, make a call to the backend to see if this value returns any results
    else if (field.getType().equals("autocomplete")) {

      // FIXME: currently, pasting an invalid value into an autocomplete field won't trigger an error.

      // If no results are found in the source function, then this field will have been marked as invalid.
//      return !(point.invalidFields && point.invalidFields.indexOf(field.id) > -1);
      return true;
    }

    else {
      return true;
    }
  }

}
