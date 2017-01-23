package cern.modesti.workflow.validation;

import cern.modesti.request.Request;
import cern.modesti.point.Point;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Constraint;
import cern.modesti.schema.field.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public class Constraints {

  public static boolean validate(Constraint constraint, Request request, Category category) {
    boolean valid = true;

    switch (constraint.getType()) {
      case "or":
        if (!validateOrConstraint(constraint, request, category)) {
          valid = false;
        }
        break;
      case "and":
        if (!validateAndConstraint(constraint, request, category)) {
          valid = false;
        }
        break;
      case "xnor":
        if (!validateXnorConstraint(constraint, request, category)) {
          valid = false;
        }
        break;
      case "unique":
        if (!validateUniqueConstraint(constraint, request, category)) {
          valid = false;
        }
        break;
    }

    return valid;
  }

  private static boolean validateOrConstraint(Constraint constraint, Request request, Category category) {
    boolean valid = true;

    for (Point point : request.getNonEmptyPoints()) {

      // Constraints are only applied if the category is editable.
      boolean editable = Conditionals.evaluate(category.getEditable(), point, request.getStatus());
      if (!editable) {
        continue;
      }

      // Get all the fields specified as members of the constraint
      List<Field> fields = category.getFields(constraint.getMembers());

      // Get a list of fields for the constraint that are empty
      List<Field> emptyFields = point.getEmptyFields(fields);

      if (emptyFields.size() == constraint.getMembers().size()) {
        point.setValid(false);
        valid = false;
        // TODO: set category to invalid
        // point.properties.valid = category.valid = valid = false;

        List<String> fieldNames = category.getFieldNames(constraint.getMembers());

        for (Field emptyField : emptyFields) {
          point.addErrorMessage(category.getId(), emptyField.getId(), "At least one of '" + String.join(", ", fieldNames)
              + "' is required for group '" + category.getName() + "'");
        }
      }
    }

    return valid;
  }

  private static boolean validateAndConstraint(Constraint constraint, Request request, Category category) {
    boolean valid = true;

    for (Point point : request.getNonEmptyPoints()) {

      // Constraints are only applied if the category is editable.
      boolean editable = Conditionals.evaluate(category.getEditable(), point, request.getStatus());
      if (!editable) {
        continue;
      }

      // Get all the fields specified as members of the constraint
      List<Field> fields = category.getFields(constraint.getMembers());

      // Get a list of fields for the constraint that are empty
      List<Field> emptyFields = point.getEmptyFields(fields);

      if (emptyFields.size() > 0) {
        point.setValid(false);
        valid = false;
        // TODO: set category to invalid
        // point.properties.valid = category.valid = valid = false;

        for (Field emptyField : emptyFields) {
          point.addErrorMessage(category.getId(), emptyField.getId(),
              "'" + emptyField.getName() + "' is required for group '" + category.getName() + "'");
        }
      }
    }

    return valid;
  }

  private static boolean validateXnorConstraint(Constraint constraint, Request request, Category category) {
    boolean valid = true;

    for (Point point : request.getNonEmptyPoints()) {

      // Constraints are only applied if the category is editable.
      boolean editable = Conditionals.evaluate(category.getEditable(), point, request.getStatus());
      if (!editable) {
        continue;
      }

      // Get all the fields specified as members of the constraint
      List<Field> fields = category.getFields(constraint.getMembers());

      // Get a list of fields for the constraint that are empty
      List<Field> emptyFields = point.getEmptyFields(fields);

      if (emptyFields.size() != 0 && emptyFields.size() != constraint.getMembers().size()) {
        point.setValid(false);
        valid = false;
        // TODO: set category to invalid
        // point.properties.valid = category.valid = valid = false;

        for (Field emptyField : emptyFields) {
          point.addErrorMessage(category.getId(), emptyField.getId(),
              "'" + emptyField.getName() + "' is required for group '" + category.getName() + "'");
        }
      }
    }

    return valid;


  }

  /**
   * Unique constraints apply to the entire request. For example, a constraint
   * with two members means that the result of the concatenation of the values
   * of those members must be unique for all points.
   */
  private static boolean validateUniqueConstraint(Constraint constraint, Request request, Category category) {
    boolean valid = true;
    List<String> concatenatedValues = new ArrayList<>();

    // Build a new array containing the concatenation of the values of all constraint members
    for (Point point : request.getNonEmptyPoints()) {
      String concatenatedValue = "";
      boolean atLeastOneNullMember = false;

      for (String member : constraint.getMembers()) {
        Object value = point.getValueByPropertyName(member);
        if (value != null && (value instanceof String && !((String) value).isEmpty())) {
          concatenatedValue += value.toString();
        } else {
          atLeastOneNullMember = true;
        }
      }

      if (atLeastOneNullMember) {
        concatenatedValue = "";
      }

      concatenatedValues.add(concatenatedValue);
    }

    for (Point point : request.getNonEmptyPoints()) {
      String value = concatenatedValues.get(request.getPoints().indexOf(point));

      if (value != null && !value.isEmpty() && concatenatedValues.indexOf(value) != concatenatedValues.lastIndexOf(value)) {
        point.setValid(false);
        valid = false;
        // TODO: set category to invalid
        // point.properties.valid = category.valid = valid = false;

        List<String> fieldNames = category.getFieldNames(constraint.getMembers());

        if (constraint.getMembers().size() == 1) {
          point.addErrorMessage(category.getId(), constraint.getMembers().get(0),
              "'" + fieldNames.get(0) + "' must be unique for all points. Check for duplications.");
        } else {
          point.addErrorMessage(category.getId(), "",
              "" + String.join(", ", fieldNames) + "' must be unique for all points. Check for duplications.");
        }
      }
    }

    return valid;
  }
}
