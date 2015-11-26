package cern.modesti.request;

import cern.modesti.request.point.Point;
import cern.modesti.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.persistence.Version;
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
  private String requestId;

  @Indexed
  private String parentRequestId;

  private List<String> childRequestIds = new ArrayList<>();

  @Indexed
  private String status;

  @Indexed
  @NotNull(message = "Request type is compulsory")
  private RequestType type;

  @Indexed
  @NotNull(message = "Request creator is compulsory")
  private User creator;

  @Indexed
  @NotNull(message = "Description is compulsory")
  private String description;

  @Indexed
  @NotNull(message = "Domain is compulsory")
  private String domain;

  // TODO: remove this from the core request. Maybe add it as a field attached to the request itself
  @Indexed
  @NotNull(message = "Subsystem is compulsory")
  private String subsystem;

  @Indexed
  private User assignee;

  @Valid
  private List<Point> points = new ArrayList<>();

  private List<Comment> comments = new ArrayList<>();

  private DateTime createdAt;

  @Version
  private Long version;

  /**
   * Custom properties
   */
  private Map<String, Object> properties = new HashMap<>();

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
    this.points = request.points;
  }

  /**
   *
   * @param key
   * @param klass
   * @param <T>
   * @return
   */
  public <T> T getObjectProperty(String key, Class<T> klass) {
    Object value = properties.get(key);
    return new ObjectMapper().convertValue(value, klass);
  }
}
