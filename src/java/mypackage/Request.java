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
package mypackage;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Justin Lewis Salmon
 */
@Document
public class Request {

  @Id
  private String id;

  @NotNull(message = "Request type is compulsory")
  private String type;

  @NotNull(message = "Description is compulsory")
  private String description;

  @NotNull(message = "Domain is compulsory")
  private String domain;

  @NotNull(message = "Data source is compulsory")
  private String datasource;

  private List<Point> points = new ArrayList<>();

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
