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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import cern.modesti.security.ldap.User;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import cern.modesti.model.SubSystem;
import cern.modesti.request.point.Point;
import cern.modesti.workflow.AddressingResult;
import cern.modesti.workflow.ConfigurationResult;
import cern.modesti.workflow.TestResult;
import cern.modesti.repository.jpa.validation.ValidationResult;

/**
 * @author Justin Lewis Salmon
 */
@Document
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
  @TextIndexed(weight = 3)
  private String requestId;

  @TextIndexed
  private String parentRequestId;

  @TextIndexed
  private List<String> childRequestIds = new ArrayList<>();

  @TextIndexed
  private RequestStatus status;

  @TextIndexed
  @NotNull(message = "Request type is compulsory")
  private RequestType type;

  @TextIndexed
  @NotNull(message = "Request creator is compulsory")
  private User creator;

  @TextIndexed(weight = 2)
  @NotNull(message = "Description is compulsory")
  private String description;

  @TextIndexed
  @NotNull(message = "Domain is compulsory")
  private String domain;

  @TextIndexed
  @NotNull(message = "Subsystem is compulsory")
  private SubSystem subsystem;

  @TextIndexed
  @NotNull(message = "At least one category is compulsory")
  private List<String> categories = new ArrayList<>();

  @TextIndexed
  @Valid
  private List<Point> points = new ArrayList<>();

  @TextIndexed
  private List<Comment> comments = new ArrayList<>();

  /**
   *
   */
  private ValidationResult validationResult;

  /**
   *
   */
  private Boolean approved;

  /**
   *
   */
  private AddressingResult addressingResult;

  /**
   *
   */
  private ConfigurationResult configurationResult;

  /**
   *
   */
  private TestResult testResult;

  /**
   *
   */
  @TextScore
  private Float score;

  /**
   *
   * @author Justin Lewis Salmon
   */
  public enum RequestStatus {
    IN_PROGRESS,
    FOR_CORRECTION,
    FOR_APPROVAL,
    FOR_ADDRESSING,
    FOR_CABLING,
    FOR_CONFIGURATION,
    CONFIGURED,
    FOR_TESTING,
    CLOSED
  }

  /**
   * Default constructor
   */
  public Request() {
  }

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
    this.subsystem = request.subsystem;
    this.categories = request.categories;
    this.points = request.points;
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
  public String getParentRequestId() {
    return parentRequestId;
  }

  /**
   *
   * @param parentRequestId
   */
  public void setParentRequestId(String parentRequestId) {
    this.parentRequestId = parentRequestId;
  }

  /**
   *
   * @return
   */
  public List<String> getChildRequestIds() {
    return childRequestIds;
  }

  /**
   *
   * @param childRequestIds
   */
  public void setChildRequestIds(List<String> childRequestIds) {
    this.childRequestIds = childRequestIds;
  }

  /**
   *
   * @return
   */
  public RequestStatus getStatus() {
    return status;
  }

  /**
   *
   * @param status
   */
  public void setStatus(RequestStatus status) {
    this.status = status;
  }

  /**
   * @return the type
   */
  public RequestType getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(RequestType type) {
    this.type = type;
  }

  /**
   * @return the creator
   */
  public User getCreator() {
    return creator;
  }

  /**
   * @param creator the creator to set
   */
  public void setCreator(User creator) {
    this.creator = creator;
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
   * @return the subsystem
   */
  public SubSystem getSubsystem() {
    return subsystem;
  }

  /**
   * @param subsystem the subsystem to set
   */
  public void setSubsystem(SubSystem subsystem) {
    this.subsystem = subsystem;
  }

  /**
   * @return the categories
   */
  public List<String> getCategories() {
    return categories;
  }

  /**
   * @param categories the categories to set
   */
  public void setCategories(List<String> categories) {
    this.categories = categories;
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

  /**
   * @return the comments
   */
  public List<Comment> getComments() {
    return comments;
  }

  /**
   * @param comments the comments to set
   */
  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }

  /**
   * @return the validationResult
   */
  public ValidationResult getValidationResult() {
    return validationResult;
  }

  /**
   * @param validationResult the validationResult to set
   */
  public void setValidationResult(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  /**
   * @return the approved
   */
  public Boolean isApproved() {
    return approved;
  }

  /**
   * @param approved the approved to set
   */
  public void setApproved(Boolean approved) {
    this.approved = approved;
  }

  /**
   * @return the addressingResult
   */
  public AddressingResult getAddressingResult() {
    return addressingResult;
  }

  /**
   * @param addressingResult the addressingResult to set
   */
  public void setAddressingResult(AddressingResult addressingResult) {
    this.addressingResult = addressingResult;
  }

  /**
   * @return the configurationResult
   */
  public ConfigurationResult getConfigurationResult() {
    return configurationResult;
  }

  /**
   * @param configurationResult the configurationResult to set
   */
  public void setConfigurationResult(ConfigurationResult configurationResult) {
    this.configurationResult = configurationResult;
  }

  /**
   * @return the testResult
   */
  public TestResult getTestResult() {
    return testResult;
  }

  /**
   * @param testResult the testResult to set
   */
  public void setTestResult(TestResult testResult) {
    this.testResult = testResult;
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
   * TODO: implement this properly
   *
   * @return true if this request requires cabling, false otherwise
   */
  public boolean requiresCabling() {
    // Return a random result for now
    return true; // new Random(System.currentTimeMillis()).nextBoolean();
  }
}
