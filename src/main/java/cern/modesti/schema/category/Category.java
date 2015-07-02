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
package cern.modesti.schema.category;

import java.util.List;

import javax.persistence.Id;

import cern.modesti.schema.field.Field;

/**
 * @author Justin Lewis Salmon
 */
public class Category {

  /**
   *
   */
  @Id
  private String name;

  /**
   * Enumerates the workflow states for which this category is disabled.
   */
  private List<String> disabledStates;

  /**
   * Enumerates the workflow states for which this category is editable.
   */
  private List<String> editableStates;

  /**
   *
   */
  private List<Constraint> constraints;

  /**
   *
   */
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
   * @return the disabledStates
   */
  public List<String> getDisabledStates() {
    return disabledStates;
  }

  /**
   * @param disabledStates the disabled to set
   */
  public void setDisabledStates(List<String> disabledStates) {
    this.disabledStates = disabledStates;
  }

  /**
   * @return the editableStates
   */
  public List<String> getEditableStates() {
    return editableStates;
  }

  /**
   * @param editableStates the editableStates to set
   */
  public void setEditableStates(List<String> editableStates) {
    this.editableStates = editableStates;
  }

  public List<Constraint> getConstraints() {
    return constraints;
  }

  public void setConstraints(List<Constraint> constraints) {
    this.constraints = constraints;
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

  /**
   * @param field the field to add
   */
  public void addField(Field field) {
    fields.add(field);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    Category other = (Category) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }
}
