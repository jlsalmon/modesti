package cern.modesti.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.CategoryImpl;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.category.DatasourceImpl;
import cern.modesti.schema.configuration.Configuration;
import cern.modesti.schema.configuration.ConfigurationImpl;
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
  
  @JsonProperty("alarm")
  private String alarmProperty;
  
  @JsonProperty("command")
  private String commandProperty;

  private List<Field> fields;

  @JsonDeserialize(using = CategoryDeserializer.class)
  private List<Category> categories = new ArrayList<>();

  @JsonDeserialize(contentAs = DatasourceImpl.class)
  private List<Datasource> datasources = new ArrayList<>();

  @JsonDeserialize(contentAs = CategoryImpl.class)
  private List<Category> overrides = new ArrayList<>();

  @JsonDeserialize(contentAs = DatasourceImpl.class)
  private List<Datasource> datasourceOverrides = new ArrayList<>();
  
  @JsonDeserialize(using = ConfigurationDeserializer.class)
  private Configuration configuration;

  private List<String> selectableStates;

  private List<RowCommentStateDescriptor> rowCommentStates;

  /**
   * Class constructor
   * 
   * @param id schema identifier
   * @param description schema description 
   */
  public SchemaImpl(String id, String description) {
    this.id = id;
    this.description = description;
  }

  public boolean isAbstract() {
    return isAbstract == null ? false : isAbstract;
  }


  /** 
   * Deserializer for Configuration property 
   */
  public static class ConfigurationDeserializer extends JsonDeserializer<ConfigurationImpl> {

    @Override
    public ConfigurationImpl deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      JsonNode node = parser.getCodec().readTree(parser);
      ObjectMapper mapper = new ObjectMapper();
      return mapper.treeToValue(node, ConfigurationImpl.class);
    }
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
