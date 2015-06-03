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
  private Boolean dirty = true;

  private Boolean selected = false;

  private Boolean valid;

  private Approval approval;

  private Boolean addressed;

  private Boolean cabled;

  private Boolean configured;

  private Boolean tested;

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
  public Boolean isDirty() {
    return dirty;
  }

  /**
   * @param dirty the dirty state to set
   */
  public void setDirty(Boolean dirty) {
    this.dirty = dirty;
  }

  public Boolean isSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public Boolean isValid() {
    return valid;
  }

  public void setValid(Boolean valid) {
    this.valid = valid;
  }

  public Approval getApproval() {
    return this.approval;
  }

  public void setApproval(Approval approval) {
    this.approval = approval;
  }

  public Boolean isAddressed() {
    return addressed;
  }

  public void setAddressed(Boolean addressed) {
    this.addressed = addressed;
  }

  public Boolean isCabled() {
    return cabled;
  }

  public void setCabled(Boolean cabled) {
    this.cabled = cabled;
  }

  public Boolean isConfigured() {
    return configured;
  }

  public void setConfigured(Boolean configured) {
    this.configured = configured;
  }

  public Boolean isTested() {
    return tested;
  }

  public void setTested(Boolean tested) {
    this.tested = tested;
  }

  public Boolean isAlarm() {
    // An alarm must have a priority code, therefore if it has a priority code it is an alarm.
    return properties.get("priorityCode") != null;

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
