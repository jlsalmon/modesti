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
package cern.modesti.repository.request.schema;

import java.util.List;

import cern.modesti.repository.request.schema.field.Field;

/**
 * @author Justin Lewis Salmon
 */
public class Category {

  private String name;

  private Boolean active;

  private List<Field> fields;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the active
   */
  public Boolean getActive() {
    return active;
  }

  /**
   * @param active the active to set
   */
  public void setActive(Boolean active) {
    this.active = active;
  }

  /**
   * @return the fields
   */
  public List<Field> getFields() {
    return fields;
  }

  /**
   * @param fields the fields to set
   */
  public void setFields(List<Field> fields) {
    this.fields = fields;
  }
}
