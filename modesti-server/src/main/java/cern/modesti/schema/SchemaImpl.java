package cern.modesti.schema;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.CategoryImpl;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.category.DatasourceImpl;
import cern.modesti.schema.field.Field;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a single MODESTI schema.
 *
 * @author Justin Lewis Salmon
 */
@Document(collection = "schema")
@Data
@NoArgsConstructor
public class SchemaImpl implements Schema {

  private static final long serialVersionUID = -5312446927405059667L;

  @Id
  private String id;

  private String description;

  @JsonProperty("abstract")
  @Getter(AccessLevel.NONE)
  private Boolean isAbstract;

  @JsonProperty("extends")
  private String parent;

  @JsonProperty("primary")
  private String idProperty;

  private List<Field> fields;

  @JsonDeserialize(using = CategoryDeserializer.class)
  private List<Category> categories = new ArrayList<>();

  @JsonDeserialize(contentAs = DatasourceImpl.class)
  private List<Datasource> datasources = new ArrayList<>();

  @JsonDeserialize(contentAs = CategoryImpl.class)
  private List<Category> overrides = new ArrayList<>();

  @JsonDeserialize(contentAs = DatasourceImpl.class)
  private List<Datasource> datasourceOverrides = new ArrayList<>();

  private List<String> selectableStates;

  private List<RowCommentStateDescriptor> rowCommentStates;

  public SchemaImpl(String id, String description) {
    this.id = id;
    this.description = description;
  }

  public boolean isAbstract() {
    return isAbstract == null ? false : isAbstract;
  }

  /**
   * Support specifying categories by ID as well as inline objects
   */
  public static class CategoryDeserializer extends JsonDeserializer<List<CategoryImpl>> {

    @Override
    public List<CategoryImpl> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      List<CategoryImpl> categories = new ArrayList<>();

      JsonNode list = parser.getCodec().readTree(parser);

      for (JsonNode node : list) {
        if (node.isObject()) {
          ObjectMapper mapper = new ObjectMapper();
          CategoryImpl category = mapper.treeToValue(node, CategoryImpl.class);
          categories.add(category);
        }

        else if (node.isTextual()) {
          categories.add(new CategoryImpl(node.asText()));
        }
      }

      return categories;
    }
  }
}
