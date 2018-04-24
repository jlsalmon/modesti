package cern.modesti.schema.category;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.Id;

import cern.modesti.schema.Schema;
import cern.modesti.schema.field.Field;
import cern.modesti.point.Point;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a single MODESTI category. A category exists as part
 * of a {@link Schema} and consists of a set of {@link Field}s which define
 * the properties that a {@link Point} may have.
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class CategoryImpl implements Category {

  private static final long serialVersionUID = -3539311586940536677L;

  @Id
  private String id;

  private String name;

  private String description;

  @JsonDeserialize(using = EditableDeserializer.class)
  private Map<String, Object> editable;

  /**
   * List of category IDs which are mutually exclusive with this category.
   */
  private List<String> excludes;

  private List<Constraint> constraints;

  @JsonDeserialize(using = FieldDeserializer.class)
  private List<Field> fields;

  public CategoryImpl(String id) {
    this.id = id;
  }

  /**
   * Copy constructor.
   *
   * @param category the category to copy
   */
  public CategoryImpl(CategoryImpl category) {
    id = category.id;
    name = category.name;
    editable = category.editable;
    constraints = category.constraints == null ? null : new ArrayList<>(category.constraints);
    fields = category.fields == null ? null : new ArrayList<>(category.fields);
  }

  public List<Field> getFields(List<String> fieldIds) {
    return fields.stream().filter(field -> fieldIds.contains(field.getId())).collect(Collectors.toList());
  }

  public List<String> getFieldNames(List<String> fieldIds) {
    return getFields(fieldIds).stream().map(Field::getName).collect(Collectors.toList());
  }

  /**
   * Support specifying categories by ID as well as inline objects
   */
  public static class FieldDeserializer extends JsonDeserializer<List<Field>> {

    @Override
    public List<Field> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      List<Field> fields = new ArrayList<>();

      JsonNode list = parser.getCodec().readTree(parser);

      for (JsonNode node : list) {
        if (node.isObject()) {
          ObjectMapper mapper = new ObjectMapper();
          Field field = mapper.treeToValue(node, Field.class);
          fields.add(field);
        }

        else if (node.isTextual()) {
          fields.add(new Field(node.asText()));
        }
      }

      return fields;
    }
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
    CategoryImpl other = (CategoryImpl) obj;
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
