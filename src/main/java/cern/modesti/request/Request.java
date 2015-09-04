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
package cern.modesti.request;

import cern.modesti.repository.subsystem.SubSystem;
import cern.modesti.request.point.state.Addressing;
import cern.modesti.request.point.state.Approval;
import cern.modesti.request.point.Point;
import cern.modesti.request.point.state.Cabling;
import cern.modesti.request.point.state.Testing;
import cern.modesti.security.ldap.User;
import cern.modesti.workflow.result.ConfigurationResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * @author Justin Lewis Salmon
 */
@Document
@Data
@NoArgsConstructor
public class Request implements Serializable {

  private static final long serialVersionUID = -7075036449830835583L;

  /**
   * Internal mongodb id
   */
  @Id
  private String id;

  /**
   * Human-readable id
   */
  @Indexed
  @TextIndexed(weight = 3)
  private String requestId;

  @TextIndexed
  private String parentRequestId;

  @TextIndexed
  private List<String> childRequestIds = new ArrayList<>();

  @Indexed
  @TextIndexed
  private RequestStatus status;

  @Indexed
  @TextIndexed
  @NotNull(message = "Request type is compulsory")
  private RequestType type;

  @Indexed
  @TextIndexed
  @NotNull(message = "Request creator is compulsory")
  private User creator;

  @Indexed
  @TextIndexed(weight = 2)
  @NotNull(message = "Description is compulsory")
  private String description;

  @Indexed
  @TextIndexed
  @NotNull(message = "Domain is compulsory")
  private String domain;

//  @Indexed
//  @TextIndexed
//  @NotNull(message = "Subsystem is compulsory")
//  private String subsystem;

  @Indexed
  @TextIndexed
  @NotNull(message = "At least one category is compulsory")
  private Set<String> categories = new HashSet<>();

  @Valid
  private List<Point> points = new ArrayList<>();

  @TextIndexed
  private List<Comment> comments = new ArrayList<>();

  /**
   *
   */
  private Boolean valid;

  /**
   *
   */
  private Approval approval = new Approval();

  /**
   *
   */
  private Addressing addressing = new Addressing();

  /**
   *
   */
  private Cabling cabling = new Cabling();

  /**
   *
   */
  private Testing testing = new Testing();

  /**
   *
   */
  private ConfigurationResult configurationResult;

  /**
   *
   */
  @TextScore
  private Float score;

  /**
   * Copy constructor
   *
   * @param request
   */
  public Request(Request request) {
    this.requestId = request.requestId;
    this.parentRequestId = request.parentRequestId;
    this.childRequestIds = request.childRequestIds;
    this.status = request.status;
    this.type = request.type;
    this.creator = request.creator;
    this.description = request.description;
    this.domain = request.domain;
//    this.subsystem = request.subsystem;
    this.categories = request.categories;
    this.points = request.points;
  }

  /**
   * @return true if this request requires approval (i.e. contains alarms), false otherwise
   */
  public boolean requiresApproval() {
    for (Point point : points) {
      if (point.isAlarm()) {
        return true;
      }
    }

    return false;
  }

  /**
   * TODO remove this TIM/CSAM specific logic from the core
   *
   * For TIM requests, a point requires cabling if:
   *  - it is an APIMMD point
   *
   * For CSAM requests, a point requires cabling if:
   *  - it is an APIMMD point or;
   *  - it is an LSAC point
   *
   * @return true if this request requires cabling, false otherwise
   */
  public boolean requiresCabling() {
    for (Point point : points) {
      String pointType = (String) point.getProperties().get("pointType");

      if (pointType != null) {
        if (pointType.equals("PLC") || pointType.equals("APIMMD") || pointType.equals("LSAC")) {
          return true;
        }
      }
    }

    return false;
  }
}
