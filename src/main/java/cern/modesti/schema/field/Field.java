package cern.modesti.schema.field;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
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
    @Type(value = AutocompleteField.class, name = "autocomplete")})
@JsonInclude(Include.NON_NULL)
public class Field implements Serializable {

  private static final long serialVersionUID = 728578311444988751L;

  @Id
  private String id;

  private String name_en;

  private String name_fr;

  private String type;

  /**
   * If a field is an object, this specifies the property of the object to display
   */
  private String model;

  private Integer minLength;

  private Integer maxLength;

  private Object required;

  private Object editable;

  private Object unique;

  private String help_en = "";

  private String help_fr = "";

  public Field(String id) {
    this.id = id;
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
