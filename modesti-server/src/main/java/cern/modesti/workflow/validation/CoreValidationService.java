package cern.modesti.workflow.validation;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.point.Point;
import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import cern.modesti.request.RequestType;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaImpl;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.schema.UnknownSchemaException;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Constraint;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.field.Option;
import cern.modesti.schema.field.OptionsField;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class CoreValidationService implements ValidationService {

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private RequestService requestService;

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private Environment environment;

  @Autowired
  private ApplicationContext context;

  private ObjectMapper mapper = new ObjectMapper();

  
  @Override
  public boolean preValidateRequest(Request request) {
    RequestValidator validator = getPluginRequestValidator(request);
    if (validator == null) {
      log.info(format("Custom validator not provided for request #%s", request.getRequestId()));
      return true;
    }
    
    return validator.preValidateRequest(request, getSchema(request.getDomain()));
  }
  
  private SchemaImpl getSchema(String id) {
    Optional<SchemaImpl> schemaOpt = schemaRepository.findById(id);
    if (!schemaOpt.isPresent()) {
      throw new UnknownSchemaException(id);
    }
    return schemaOpt.get();
  }

  @Override
  public boolean validateRequest(Request request) {
    try {
      boolean valid = true;
      Schema schema = getSchema(request.getDomain());
      // Reset all points and clear any error messages.
      for (Point point : request.getPoints()) {
        point.setValid(true);
        point.setErrors(new ArrayList<>());
      }

      if (environment.getProperty("modesti.disableValidator", Boolean.class, false) ||
          request.isSkipCoreValidation()) {
        log.info("core validations disabled");
      } else {

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
      }

      request.setValid(valid);
      requestService.save(request);

      if (!valid) {
        log.info(format("request #%s failed validation, not invoking custom validator", request.getRequestId()));
        return false;
      }

      log.info(format("request #%s is valid, invoking custom validator", request.getRequestId()));

      RequestValidator validator = getPluginRequestValidator(request);
      if (validator == null) {
        log.info(format("custom validator not provided for request #%s", request.getRequestId()));
        return true;
      }

      valid = validator.validateRequest(request, schema);
      request.setValid(valid);
      requestService.save(request);

      return valid;
    }catch(RuntimeException e){
      request.setValid(false);
      requestService.save(request);
      log.info(format("Unexpected error during validation phase for request #%s '%s'", request.getRequestId(), e.toString()), e);
      return false;
    }
  }

  private RequestValidator getPluginRequestValidator(Request request) {
    RequestProvider plugin = requestProviderRegistry.getPluginFor(request);
    String pluginId = plugin.getMetadata().getId();
    for (RequestValidator validator : context.getBeansOfType(RequestValidator.class).values()) {
      if (validator.getPluginId().equals(pluginId)) {
        return validator;
      }
    }
    return null;
  }


  private boolean validatePoints(Request request, List<Category> categories) {
    boolean valid = true;

    for (Point point : request.getNonEmptyPoints()) {

      valid &= validateTagName(point);
      
      for (Category category : categories) {
        Map<String, Object> editable = category.getEditable();
        if (editable.containsKey("status")) {
          editable.remove("status");
        }
        if (!Conditionals.evaluate(category.getEditable(), point, request)) {
          log.trace("The category {} will not be evaluated (not relevant)", category.getName());
          continue;
        }
        
        for (Field field : category.getFields()) {

          Object value = point.getValueByPropertyName(field.getPropertyName());

          // Check for invalid fields
          if (!isValidValue(value, point, field)) {
            point.setValid(false);
            valid = false;
            point.addErrorMessage(category.getId(), field.getId(), "Value '" + value + "' is not a legal option for field '" + field.getName()
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
            required = Conditionals.evaluate(field.getRequired(), point, request);
          }

          if (required && (value == null || "".equals(value))) {
            point.setValid(false);
            valid = false;
            point.addErrorMessage(category.getId(), field.getId(), "'" + field.getName() + "' is mandatory");
          }

          // Min length
          if (field.getMinLength() != null && (value != null && value.toString().length() < field.getMinLength())) {
            point.setValid(false);
            valid = false;
            point.addErrorMessage(category.getId(), field.getId(),
                "'" + field.getName() + "' must be at least " + field.getMinLength() + " characters in length");
          }

          // Max length
          if (field.getMaxLength() != null && (value != null && value.toString().length() > field.getMaxLength())) {
            point.setValid(false);
            valid = false;
            point.addErrorMessage(category.getId(), field.getId(),
                "'" + field.getName() + "' must not exceed " + field.getMaxLength() + " characters in length");
          }

          // Numeric fields
          if ("numeric".equals(field.getType()) && (value != null && !value.toString().isEmpty() && !NumberUtils.isNumber(value.toString()))) {
            point.setValid(false);
            valid = false;
            point.addErrorMessage(category.getId(), field.getId(), "Value for '" + field.getName() + "' must be numeric");
          }
        }
      }
    }

    return valid;
  }

  private boolean validateTagName(Point point) {
    String tagname = point.getProperty("tagname", String.class);
    
    if (StringUtils.containsWhitespace(tagname)) {
      point.setValid(false);
      point.addErrorMessage("general", "tagname", "The field 'Tagname' cannot contain whitespace characters");
      return false;
    }
    
    return true;
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
            for (Point point : request.getNonEmptyPoints()) {
              List<Field> emptyFields = point.getEmptyFields(category.getFields());

              // If at least one of the fields of this category are filled, then we must check the excluded category.
              if (emptyFields.size() != category.getFields().size()) {
                emptyFields = point.getEmptyFields(excludedCategory.getFields());

                if (emptyFields.size() != excludedCategory.getFields().size()) {
                  point.setValid(false);
                  valid = false;
                  point.addErrorMessage(category.getId(), "", "Fields in the '" + category.getName() + "' group cannot be used if fields in the '"
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
    if (value ==  null || "".equals(value) || Boolean.TRUE.equals(field.getSearchFieldOnly())) {
      return true;
    }

    // If we have an options field, check that the value is in the list of options
    if ("options".equals(field.getType()))  {
      OptionsField optionsField = (OptionsField) field;
      List<Object> options = (List<Object>) optionsField.getOptions();

      if (options != null) {
        for (Object option : options) {
          option = option instanceof Map ? mapper.convertValue(option, Option.class).getValue() : option;

          if ((option instanceof Number) && (NumberUtils.isNumber(value.toString()))) {
            if (new Double(option.toString()).equals(new Double(value.toString()))) {
              return true;
            }
          } else if (option.equals(value)) {
            return true;
          }
        }
      }

      return false;
    } else if ("autocomplete".equals(field.getType())) {
      // Otherwise, if we have an autocomplete field, make a call to the backend to see if this value returns any results
      // FIXME: currently, pasting an invalid value into an autocomplete field won't trigger an error.
      return true;
    }

    return true;
  }

}
