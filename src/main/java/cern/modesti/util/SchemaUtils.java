package cern.modesti.util;

import cern.modesti.request.point.Point;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.field.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Justin Lewis Salmon
 */
public class SchemaUtils {

  public static List<Field> getFields(Category category, List<String> fieldIds) {
    return category.getFields().stream().filter(field -> fieldIds.contains(field.getId())).collect(Collectors.toList());
  }

  public static List<String> getFieldNames(Category category, List<String> fieldIds) {
    return getFields(category, fieldIds).stream().map(Field::getName).collect(Collectors.toList());
  }

  /**
   * Get the full property name of the given field. For object-type properties,
   * this will generally be the field id + the model value (e.g. gmaoCode.value)
   *
   * @param field the field to access
   *
   * @return the property name
   */
  public static String getPropertyName(Field field) {
    if (field.getType().equals("autocomplete")) {
      return field.getId() + "." + (field.getModel() != null ? field.getModel() : "value");
    } else {
      return field.getId();
    }
  }

  public static List<Field> getEmptyFields(Point point, List<Field> fields) {
    List<Field> emptyFields = new ArrayList<>();

    for (Field field : fields) {
      Object value = PointUtils.getValueByPropertyName(point, getPropertyName(field));
      if (value == null || (value instanceof String && ((String) value).isEmpty())) {
        emptyFields.add(field);
      }

      // TODO: remove this domain-specific code
      // TODO: possibly replace with a "generated" field option which would cause it to be ignored
      // TODO: or a "multiple" option which would do the same... or both

      // HACK ALERT: treat auto-generated fields as "empty"
      if (field.getId().equals("tagname") || field.getId().equals("faultFamily") ||
          field.getId().equals("faultMember") || field.getId().equals("pointDescription")) {
        if (!emptyFields.contains(field)) {
          emptyFields.add(field);
        }
      }

      // HACK ALERT #2: ignore the monitoringEquipment field because it can be in multiple categories...
      if (field.getId().equals("monitoringEquipment")) {
        if (!emptyFields.contains(field)) {
          emptyFields.add(field);
        }
      }
    }

    return emptyFields;
  }
}
