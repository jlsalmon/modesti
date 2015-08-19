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
import lombok.Data;


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
@Data
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

  private Boolean required;

  private Boolean editable;

  private String help_en = "";

  private String help_fr = "";
}
