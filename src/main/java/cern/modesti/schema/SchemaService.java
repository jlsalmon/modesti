package cern.modesti.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cern.modesti.schema.options.OptionService;
import cern.modesti.schema.category.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.request.Request;
import cern.modesti.schema.field.Field;

@Service
public class SchemaService {

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private OptionService optionService;

  /**
   * TODO rewrite this and explain it better
   *
   * The schema hierarchy looks like the following:
   *
   *
   * core                                                                  [core]
   * |
   * +--------------------------+-----------------------------------+
   * |                          |                                   |
   * tim                        csam                               wincc   [domains]
   * |                          |                                   |
   * +-----+-----+-----+        +--------+--------+--------+        +
   * |     |     |     |        |        |        |        |        |
   * plc   opc   japc  ...      lsac   winter  securiton   ...     ...     [categories]
   *
   * Each domain has a schema which inherits from the core schema. Each domain has a number of associated category schemas which inherit from their domain schema. When a schema
   * is materialised, it is merged its parents, and optionally its siblings if they were specified.
   *
   * Note: it is not possible to merge a category from another domain into a schema.
   *
   *
   * @param request
   * @param categories
   *
   * @return
   */
  public Schema getSchema(Request request) {
    Schema schema = schemaRepository.findOne(request.getDomain());
    schema.setName(request.getDescription());
    return schema;
  }

  /**
   * Merge schema b into schema a.
   *
   * @param a
   * @param b
   *
   * @return
   */
//  private Schema mergeSchema(Schema a, Schema b) {
//    Set<Category> categories = a.getCategories();
//    List<Category> newCategories = new ArrayList<>();
//
//    for (Category newCategory : b.getCategories()) {
//      if (!categories.contains(newCategory)) {
//        newCategories.add(newCategory);
//
//      } else {
//        Category category = categories.get(categories.indexOf(newCategory));
//        List<Field> newFields = new ArrayList<>();
//
//        for (Field newField : newCategory.getFields()) {
//          if (!category.getFields().contains(newField)) {
//            newFields.add(newField);
//          }
//        }
//
//        // Copy the disabled state list if the child doesn't specify it.
//        if (newCategory.getDisabledStates() != null && category.getDisabledStates() == null) {
//          category.setDisabledStates(newCategory.getDisabledStates());
//        }
//
//        // Copy the editable state list if the child doesn't specify it.
//        if (newCategory.getEditableStates() != null && category.getEditableStates() == null) {
//          category.setEditableStates(newCategory.getEditableStates());
//        }
//
//        // Copy the constraint list
//        if (newCategory.getConstraints() != null && category.getConstraints() == null) {
//          category.setConstraints(newCategory.getConstraints());
//        }
//
//        category.getFields().addAll(newFields);
//      }
//    }
//
//    a.getCategories().addAll(newCategories);
//    return a;
//  }
}
