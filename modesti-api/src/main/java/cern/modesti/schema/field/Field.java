package cern.modesti.schema.field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * This class represents a single field of a
 * {@link cern.modesti.schema.category.Category}. A field defines the structure
 * of a property that a {@link cern.modesti.point.Point} may have.
 *
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
    @Type(value = TextField.class, name = "text"),
    @Type(value = NumericField.class, name = "numeric"),
    @Type(value = CheckboxField.class, name = "checkbox"),
    @Type(value = OptionsField.class, name = "options"),
    @Type(value = AutocompleteField.class, name = "autocomplete"),
    @Type(value = EmailField.class, name = "email"),
    @Type(value = DateField.class, name = "date")})
@JsonInclude(Include.NON_NULL)
public class Field implements Serializable {

  private static final long serialVersionUID = 728578311444988751L;

  private String id;

  private String name;

  private String type;

  /**
   * If a field is an object, this specifies the property of the object to display.
   *
   * TODO: move this into {@link AutocompleteField}
   */
  private String model;

  private Integer minLength;

  private Integer maxLength;

  private Object required;

  private Object editable;
  /** Defines fields visible on certain workflow statuses **/
  private Object visibleOnStatus;

  private Object unique;

  private Object template;

  private Object fixed;

  @JsonProperty("default")
  private Object defaultValue;

  private String help = "";

  private String helpUrl = "";
  
  private Boolean searchFieldOnly;
  
  private Object filters;

  public Field(String id) {
    this.id = id;
  }

  /**
   * Get the full property name of the given field. For object-type properties,
   * this will generally be the field id + the model value (e.g. gmaoCode.value)
   *
   * @return the property name
   */
  public String getPropertyName() {
    if ("autocomplete".equals(type)) {
      return id + "." + (model != null ? model : "value");
    } else {
      return id;
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
    if (!(obj instanceof Field)) {
      return false;
    }
    Field other = (Field) obj;
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