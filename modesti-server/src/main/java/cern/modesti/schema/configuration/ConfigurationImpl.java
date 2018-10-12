package cern.modesti.schema.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Schema configuration options
 *  
 * @author Ivan Prieto Barreiro
 */
@Data
public class ConfigurationImpl implements Configuration {
  private static final long serialVersionUID = -4884546544907353282L;
  
  @JsonProperty("createFromUi")
  private boolean createFromUi = true;
  
  @JsonProperty("cloneFromUi")
  private boolean cloneFromUi = true;
  
  @JsonProperty("showFieldsOnClone")
  private boolean showFieldsOnClone;

  @JsonProperty("showFieldsOnDelete")
  private boolean showFieldsOnDelete;
}
