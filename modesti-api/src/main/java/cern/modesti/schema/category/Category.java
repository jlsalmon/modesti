package cern.modesti.schema.category;

import cern.modesti.schema.field.Field;

import java.util.Map;
import java.io.Serializable;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface Category extends Serializable {

  String getId();

  void setId(String id);

  String getName();

  void setName(String name);

  List<Field> getFields();

  List<Field> getFields(List<String> fieldIds);

  void setFields(List<Field> fields);

  List<String> getExcludes();

  void setExcludes(List<String> excludes);

  List<Constraint> getConstraints();

  void setConstraints(List<Constraint> constraints);

  Map<String, Object> getEditable();

  void setEditable(Map<String, Object> editables);

  List<String> getFieldNames(List<String> fieldIds);
}
