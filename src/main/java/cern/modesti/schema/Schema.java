package cern.modesti.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;

import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.field.Field;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Justin Lewis Salmon
 */
@Document
@Data
@NoArgsConstructor
public class Schema {

  @Id
  private String id;

  private String description;

  @JsonProperty("abstract")
  @Getter(AccessLevel.NONE)
  private Boolean isAbstract;

  @JsonProperty("extends")
  private String parent;

  private List<Field> fields;

  @JsonDeserialize(using = CategoryDeserializer.class)
  private List<Category> categories = new ArrayList<>();

  private List<Datasource> datasources = new ArrayList<>();

  private List<Category> overrides = new ArrayList<>();

  private List<Datasource> datasourceOverrides = new ArrayList<>();

  /**
   * @param id
   * @param description
   */
  public Schema(String id, String description) {
    this.id = id;
    this.description = description;
  }

  /**
   *
   * @return
   */
  public Boolean isAbstract() {
    return isAbstract == null ? false : isAbstract;
  }

  /**
   *
   */
  public static class CategoryDeserializer extends JsonDeserializer<List<Category>> {

    @Override
    public List<Category> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      List<Category> categories = new ArrayList<>();

      JsonNode list = parser.getCodec().readTree(parser);

      for (JsonNode node : list) {
        if (node.isObject()) {
          ObjectMapper mapper = new ObjectMapper();
          Category category = mapper.treeToValue(node, Category.class);
          categories.add(category);
        }

        else if (node.isTextual()) {
          categories.add(new Category(node.asText()));
        }
      }

      return categories;
    }
  }
}
