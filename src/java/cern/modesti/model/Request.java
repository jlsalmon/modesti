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
package cern.modesti;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

/**
 * @author Justin Lewis Salmon
 */
@Document
public class Request {

  /**
   * Internal mongodb id
   */
  @Id
  private String id;

  /**
   * Human-readable id
   */
  @TextIndexed(weight = 3)
  private String requestId;

  @TextIndexed
  private String status;

  @TextIndexed
  @NotNull(message = "Request type is compulsory")
  private String type;

  @TextIndexed(weight = 2)
  @NotNull(message = "Description is compulsory")
  private String description;

  @TextIndexed
  @NotNull(message = "Domain is compulsory")
  private String domain;

  @TextIndexed
  @NotNull(message = "Data source is compulsory")
  private String datasource;

  @TextIndexed
  @Valid
  private List<Point> points = new ArrayList<>();

  /**
   *
   */
  @TextScore
  private Float score;

  public interface RequestStatus {
    String IN_PROGRESS = "in progress";
    String FOR_CORRECTION = "for correction";
    String FOR_APPROVAL = "for approval";
    String FOR_ADDRESSING = "for addressing";
    String FOR_CABLING = "for cabling";
    String FOR_CONFIGURATION = "for configuration";
    String CONFIGURED = "configured";
    String FOR_TESTING = "for testing";
    String CLOSED = "closed";
  }

  /**
   *
   */
  public String getId() {
    return this.id;
  }

  /**
   *
   * @return
   */
  public String getRequestId() {
    return requestId;
  }

  /**
   *
   * @param id
   */
  public void setRequestId(String id) {
    this.requestId = id;
  }

  /**
   *
   * @return
   */
  public String getStatus() {
    return status;
  }

  /**
   *
   * @param status
   */
  public void setStatus(String status) {
    this.status = status;
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
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * @return the datasource
   */
  public String getDatasource() {
    return datasource;
  }

  /**
   * @param datasource the datasource to set
   */
  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  /**
   * @return the points
   */
  public List<Point> getPoints() {
    return points;
  }

  /**
   * @param points the points to set
   */
  public void setPoints(List<Point> points) {
    this.points = points;
  }
}
