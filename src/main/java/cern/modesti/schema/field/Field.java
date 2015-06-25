/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.modesti.schema.field;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * @author Justin Lewis Salmon
 */
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
public class Field {

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

  /**
   * Can be true, false or "group" to specify that a field is mandatory if any of
   * the other fields in the group are filled in
   */
  private Object required;

  private String help_en = "";

  private String help_fr = "";

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name_en
   */
  public String getName_en() {
    return name_en;
  }

  /**
   * @param name_en the name_en to set
   */
  public void setName_en(String name_en) {
    this.name_en = name_en;
  }

  /**
   * @return the name_fr
   */
  public String getName_fr() {
    return name_fr;
  }

  /**
   * @param name_fr the name_fr to set
   */
  public void setName_fr(String name_fr) {
    this.name_fr = name_fr;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the model
   */
  public String getModel() {
    return model;
  }

  /**
   * @param model the model to set
   */
  public void setModel(String model) {
    this.model = model;
  }

  /**
   * @return the minLength
   */
  public Integer getMinLength() {
    return minLength;
  }

  /**
   * @param minLength the minLength to set
   */
  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }

  /**
   * @return the maxLength
   */
  public Integer getMaxLength() {
    return maxLength;
  }

  /**
   * @param maxLength the maxLength to set
   */
  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }

  /**
   * @return the required
   */
  public Object getRequired() {
    return required;
  }

  /**
   * @param required the required to set
   */
  public void setRequired(Object required) {
    this.required = required;
  }

  public String getHelp_en() {
    return help_en;
  }

  public void setHelp_en(String help_en) {
    this.help_en = help_en;
  }

  public String getHelp_fr() {
    return help_fr;
  }

  public void setHelp_fr(String help_fr) {
    this.help_fr = help_fr;
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
