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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import cern.modesti.schema.field.Field;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Category {

  @Id
  private String id;

  private String name_en;

  private String name_fr;

  /**
   * Enumerates the workflow states for which this category is disabled.
   */
  private List<String> disabledStates;

  /**
   * Enumerates the workflow states for which this category is editable.
   */
  private List<String> editableStates;

  /**
   * List of category IDs which are mutually exclusive with this category.
   */
  private List<String> excludes;

  private List<Constraint> constraints;

  private List<Field> fields;

  /**
   *
   * @param id
   */
  public Category(String id) {
    this.id = id;
  }

  /**
   * Copy constructor
   *
   * @param category
   */
  public Category(Category category) {
    id = category.id;
    name_en = category.name_en;
    name_fr = category.name_fr;
    disabledStates = category.disabledStates == null ? null : new ArrayList<>(category.disabledStates);
    editableStates = category.editableStates == null ? null : new ArrayList<>(category.editableStates);
    constraints = category.constraints == null ? null : new ArrayList<>(category.constraints);
    fields = category.fields == null ? null : new ArrayList<>(category.fields);
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
    if (!(obj instanceof Category)) {
      return false;
    }
    Category other = (Category) obj;
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
