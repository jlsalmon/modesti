package cern.modesti.schema;

import java.util.ArrayList;
import java.util.List;

import cern.modesti.repository.mongo.schema.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.schema.field.Field;

@Service
public class SchemaService {

  @Autowired
  private SchemaRepository schemaRepository;

  /**
   *
   * @param name
   * @return
   */
  Schema materialiseSchema(String name) {
    Schema schema = schemaRepository.findOneByName(name);

    if (schema == null) {
      return schema;
    }

    // Merge in the domain schema and all parent schemas.
    //
    // The merged schema is not saved back to the repository, it is generated
    // fresh each time. This is because we don't want to duplicate the
    // categories/fields defined in the parent schemas, and because it makes
    // editing schemas cleaner.

    Schema domain = schemaRepository.findOneByName(schema.getDomain());
    if (domain == null) {
      throw new IllegalStateException("Schema \"" + schema.getName() + "\" specifies non-existent domain schema \"" + schema.getDomain() + "\"");
    }

    schema = mergeDomainSchema(schema, domain);

    Schema parent = schemaRepository.findOneByName(domain.getParent());
    if (parent == null) {
      throw new IllegalStateException("Schema \"" + schema.getName() + "\" specifies non-existent parent schema \"" + schema.getParent() + "\"");
    }

    schema = mergeParentSchema(schema, parent);

    return schema;
  }

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

        newFields.addAll(category.getFields());
        category.setFields(newFields);
      }
    }

    newCategories.addAll(schema.getCategories());
    schema.setCategories(newCategories);

    return schema;
  }

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

        newFields.addAll(category.getFields());
        category.setFields(newFields);
      }
    }

    newCategories.addAll(schema.getCategories());
    schema.setCategories(newCategories);

    return schema;
  }
}
