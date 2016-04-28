package cern.modesti.schema.category;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import cern.modesti.schema.field.Field;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a single MODESTI category. A category exists as part
 * of a {@link cern.modesti.schema.Schema} and consists of a set of
 * {@link Field}s which define the properties that a
 * {@link cern.modesti.request.point.Point} may have.
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Category {

  @Id
  private String id;

  private String name;

  private String description;

  private Object editable;

  /**
   * List of category IDs which are mutually exclusive with this category.
   */
  private List<String> excludes;

  private List<Constraint> constraints;

  private List<Field> fields;

  public Category(String id) {
    this.id = id;
  }

  /**
   * Copy constructor.
   *
   * @param category the category to copy
   */
  public Category(Category category) {
    id = category.id;
    name = category.name;
    editable = category.editable;
    constraints = category.constraints == null ? null : new ArrayList<>(category.constraints);
    fields = category.fields == null ? null : new ArrayList<>(category.fields);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Category)) {
      return false;
    }
    Category other = (Category) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }
}
