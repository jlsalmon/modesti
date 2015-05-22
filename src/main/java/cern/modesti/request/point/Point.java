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
package cern.modesti.request.point;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;

/**
 * @author Justin Lewis Salmon
 */
public class Point implements Serializable {

  private static final long serialVersionUID = -6275036449999835583L;

  /**
   *
   */
  @Id
  private Long id;

  /**
   * Flag to check if this point has been modified by the requestor.
   */
  private boolean dirty = true;

  private boolean valid = false;

  private boolean approved = false;

  private boolean addressed = false;

  private boolean cabled = false;

  private boolean configured = false;

  private boolean tested = false;

  /**
   *
   */
  private Map<String, Object> properties = new HashMap<>();

  public Point() {
  }

  public Point(Long id) {
    this.id = id;
    this.properties.put("pointDescription", "");
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the dirty state
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * @param dirty the dirty state to set
   */
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public boolean isApproved() {
    return approved;
  }

  public void setApproved(boolean approved) {
    this.approved = approved;
  }

  public boolean isAddressed() {
    return addressed;
  }

  public void setAddressed(boolean addressed) {
    this.addressed = addressed;
  }

  public boolean isCabled() {
    return cabled;
  }

  public void setCabled(boolean cabled) {
    this.cabled = cabled;
  }

  public boolean isConfigured() {
    return configured;
  }

  public void setConfigured(boolean configured) {
    this.configured = configured;
  }

  public boolean isTested() {
    return tested;
  }

  public void setTested(boolean tested) {
    this.tested = tested;
  }

  /**
   * @return the properties
   */
  public Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * @param properties the properties to set
   */
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }
}
