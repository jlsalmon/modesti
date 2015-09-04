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

import cern.modesti.request.point.state.*;
import cern.modesti.request.point.state.Error;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
public class Point {

//  @Id
//  private String id;

  private Long lineNo;

  private Boolean dirty = true;

  private Boolean selected = false;

  private Boolean valid;

  private List<Error> errors = new ArrayList<>();

  private Approval approval = new Approval();

  private Addressing addressing = new Addressing();

  private Cabling cabling = new Cabling();

  private Boolean configured;

  private Testing testing = new Testing();

  private Map<String, Object> properties = new HashMap<>();

//  public Point(String id) {
//    this.id = id;
//  }

  public Point(Long lineNo) {
    this.lineNo = lineNo;
  }

  /**
   *
   * @return
   */
  public Boolean isAlarm() {
    // An alarm must have a priority code, therefore if it has a priority code it is an alarm.
    return properties.get("priorityCode") != null;
  }
}
