package cern.modesti.schema.category;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import cern.modesti.schema.field.Field;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Category {

  @Id
  private String id;

  private String name_en;

  private String name_fr;

  private Object editable;

  /**
   * List of category IDs which are mutually exclusive with this category.
   */
  private List<String> excludes;

  private List<Constraint> constraints;

  private List<Field> fields;

  /**
   *
   * @param id
   */
  public Category(String id) {
    this.id = id;
  }

  /**
   * Copy constructor
   *
   * @param category
   */
  public Category(Category category) {
    id = category.id;
    name_en = category.name_en;
    name_fr = category.name_fr;
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
