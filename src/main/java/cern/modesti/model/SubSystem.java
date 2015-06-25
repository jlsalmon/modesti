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
package cern.modesti.model;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * @author Justin Lewis Salmon
 */
@Entity
public class SubSystem {

  @Id
  private String value;

  private String system;

  private String systemCode;

  private String subsystem;

  private String subsystemCode;

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return the system
   */
  public String getSystem() {
    return system;
  }

  /**
   * @param system the system to set
   */
  public void setSystem(String system) {
    this.system = system;
  }

  public String getSystemCode() {
    return systemCode;
  }

  public void setSystemCode(String systemCode) {
    this.systemCode = systemCode;
  }

  /**
   * @return the subsystem
   */
  public String getSubsystem() {
    return subsystem;
  }

  /**
   * @param subsystem the subsystem to set
   */
  public void setSubsystem(String subsystem) {
    this.subsystem = subsystem;
  }

  public String getSubsystemCode() {
    return subsystemCode;
  }

  public void setSubsystemCode(String subsystemCode) {
    this.subsystemCode = subsystemCode;
  }
}
