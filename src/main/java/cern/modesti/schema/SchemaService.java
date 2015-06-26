package cern.modesti.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
  Schema materialiseSchema(Request request, List<String> categories) {
    Schema schema = new Schema(request.getRequestId(), request.getDescription(), request.getDomain());

    Schema domainSchema = schemaRepository.findOneByNameIgnoreCase(request.getDomain());
    Schema parentSchema = schemaRepository.findOneByNameIgnoreCase(domainSchema.getParent());

    // Merge the core schema
    if (parentSchema == null) {
      throw new IllegalStateException("Parent schema \"" + domainSchema.getParent() + "\" for domain " + domainSchema.getName() + " was not found");
    }
    schema = mergeParentSchema(schema, parentSchema);

    // Merge the domain schema
    if (domainSchema == null) {
      throw new IllegalStateException("Schema for domain \"" + request.getDomain() + "\" was not found");
    }
    schema = mergeDomainSchema(schema, domainSchema);

    // Merge all sibling schemas
    for (String category : categories) {
      LOG.info("finding and merging schema for category " + category);

      Schema categorySchema = schemaRepository.findOneByNameIgnoreCase(category);
      if (categorySchema == null) {
        throw new IllegalStateException("Schema for category \"" + category + "\" was not found");
      }

      schema = mergeSiblingSchema(schema, categorySchema);
    }

    return schema;
  }

  /**
   *
   * @param schema
   * @param sibling
   * @return
   */
  Schema mergeSiblingSchema(Schema schema, Schema sibling) {
    List<Category> categories = schema.getCategories();
    List<Category> newCategories = new ArrayList<>();

    for (Category siblingCategory : sibling.getCategories()) {
      if (!categories.contains(siblingCategory)) {
        newCategories.add(siblingCategory);

      } else {
        Category category = categories.get(categories.indexOf(siblingCategory));
        List<Field> newFields = new ArrayList<>();

        for (Field siblingField : siblingCategory.getFields()) {
          if (!category.getFields().contains(siblingField)) {
            newFields.add(siblingField);
          }
        }

        category.getFields().addAll(newFields);
      }
    }

    schema.getCategories().addAll(newCategories);
    return schema;
  }

  /**
   * @param schema
   * @param domain
   *
   * @return
   */
  Schema mergeDomainSchema(Schema schema, Schema domain) {
    List<Category> categories = schema.getCategories();
    List<Category> newCategories = new ArrayList<>();

    for (Category domainCategory : domain.getCategories()) {
      if (!categories.contains(domainCategory)) {
        newCategories.add(domainCategory);

      } else {
        Category category = categories.get(categories.indexOf(domainCategory));
        List<Field> newFields = new ArrayList<>();

        for (Field domainField : domainCategory.getFields()) {
          if (!category.getFields().contains(domainField)) {
            newFields.add(domainField);
          }
        }

        // Copy the parent disabled state list if the child doesn't specify it.
        if (domainCategory.getDisabledStates() != null && category.getDisabledStates() == null) {
          category.setDisabledStates(domainCategory.getDisabledStates());
        }

        // Copy the parent editable state list if the child doesn't specify it.
        if (domainCategory.getEditableStates() != null && category.getEditableStates() == null) {
          category.setEditableStates(domainCategory.getEditableStates());
        }

        category.getFields().addAll(newFields);
      }
    }

    schema.getCategories().addAll(newCategories);
    return schema;
  }

  /**
   * @param schema
   * @param parent
   *
   * @return
   */
  Schema mergeParentSchema(Schema schema, Schema parent) {
    List<Category> categories = schema.getCategories();
    List<Category> newCategories = new ArrayList<>();

    for (Category parentCategory : parent.getCategories()) {
      if (!categories.contains(parentCategory)) {
        newCategories.add(parentCategory);

      } else {
        Category category = categories.get(categories.indexOf(parentCategory));
        List<Field> newFields = new ArrayList<>();

        for (Field parentField : parentCategory.getFields()) {
          if (!category.getFields().contains(parentField)) {
            newFields.add(parentField);
          }
        }

        // Copy the parent disabled state list if the child doesn't specify it.
        if (parentCategory.getDisabledStates() != null && category.getDisabledStates() == null) {
          category.setDisabledStates(parentCategory.getDisabledStates());
        }

        // Copy the parent editable state list if the child doesn't specify it.
        if (parentCategory.getEditableStates() != null && category.getEditableStates() == null) {
          category.setEditableStates(parentCategory.getEditableStates());
        }

        category.getFields().addAll(newFields);
      }
    }

    schema.getCategories().addAll(newCategories);
    return schema;
  }
}
