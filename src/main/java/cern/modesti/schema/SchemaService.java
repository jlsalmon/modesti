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

import cern.modesti.repository.mongo.schema.SchemaRepository;
import cern.modesti.request.Request;
import cern.modesti.schema.field.Field;

@Service
public class SchemaService {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaService.class);

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
   * tim                        csam                               pvss    [domains]
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
  public Schema materialiseSchema(Request request, Set<String> categories) {
    Schema schema = new Schema(request.getRequestId(), request.getDescription(), request.getDomain());

    Schema domainSchema = schemaRepository.findOneByNameIgnoreCase(request.getDomain());
    Schema parentSchema = schemaRepository.findOneByNameIgnoreCase(domainSchema.getParent());

    // Merge the core schema
    if (parentSchema == null) {
      throw new IllegalStateException("Parent schema \"" + domainSchema.getParent() + "\" for domain " + domainSchema.getName() + " was not found");
    }
    schema = mergeSchema(schema, parentSchema);

    // Merge the domain schema
    if (domainSchema == null) {
      throw new IllegalStateException("Schema for domain \"" + request.getDomain() + "\" was not found");
    }
    schema = mergeSchema(schema, domainSchema);

    // Merge all sibling schemas
    for (String category : categories) {
      LOG.info("finding and merging schema for category " + category);

      Schema categorySchema = schemaRepository.findOneByNameIgnoreCase(category);
      if (categorySchema == null) {
        throw new IllegalStateException("Schema for category \"" + category + "\" was not found");
      }

      schema = mergeSchema(schema, categorySchema);
    }

    // Inject options
    optionService.injectOptions(schema);

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
  private Schema mergeSchema(Schema a, Schema b) {
    List<Category> categories = a.getCategories();
    List<Category> newCategories = new ArrayList<>();

    for (Category newCategory : b.getCategories()) {
      if (!categories.contains(newCategory)) {
        newCategories.add(newCategory);

      } else {
        Category category = categories.get(categories.indexOf(newCategory));
        List<Field> newFields = new ArrayList<>();

        for (Field newField : newCategory.getFields()) {
          if (!category.getFields().contains(newField)) {
            newFields.add(newField);
          }
        }

        // Copy the disabled state list if the child doesn't specify it.
        if (newCategory.getDisabledStates() != null && category.getDisabledStates() == null) {
          category.setDisabledStates(newCategory.getDisabledStates());
        }

        // Copy the editable state list if the child doesn't specify it.
        if (newCategory.getEditableStates() != null && category.getEditableStates() == null) {
          category.setEditableStates(newCategory.getEditableStates());
        }

        // Copy the constraint list
        if (newCategory.getConstraints() != null && category.getConstraints() == null) {
          category.setConstraints(newCategory.getConstraints());
        }

        category.getFields().addAll(newFields);
      }
    }

    a.getCategories().addAll(newCategories);
    return a;
  }
}