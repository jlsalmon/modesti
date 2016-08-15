package cern.modesti.request;

import cern.modesti.request.point.Point;
import cern.modesti.request.point.Error;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cern.modesti.util.PointUtils.isEmptyPoint;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * This class represents a single MODESTI request entity. A request is composed
 * of multiple {@link Point}s.
 * <p>
 * A request can hold a map of arbitrary properties. These properties can be
 * either primitive values or complex objects.
 * <p>
 * In the case of complex object properties, the
 * {@link #getProperty(String, Class)} utility method can be used to
 * retrieve them as their specific domain class instance.
 * <p>
 * For example:
 * <p>
 * <code>
 * request.addProperty("myDomainObject", new MyDomainObject());
 * MyDomainObject myDomainObject = request.getProperty("myDomainObject", MyDomainObject.class);
 * </code>
 *
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
  @NotNull(message = "Description is compulsory")
  private String description;

  @Indexed
  @NotNull(message = "Domain is compulsory")
  private String domain;

  @Indexed
  private String creator;

  @Indexed
  private String assignee;

  private Boolean valid;

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
   * Copy constructor.
   *
   * @param request the request to copy
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
    this.points = request.points;
  }

  /**
   * Retrieve a request property and convert it to the given type.
   * <p>
   * The specific domain plugin is responsible for making sure that non
   * existent properties or properties of the wrong type are not requested.
   *
   * @param key   the property key
   * @param klass the type to which to convert the value
   * @param <T>   the type of the value
   * @return the value mapped by the given key, converted to the given type
   */
  public <T> T getProperty(String key, Class<T> klass) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    Object value = properties.get(key);
    return mapper.convertValue(value, klass);
  }

  public List<Point> getPoints() {
    return this.points.stream().filter(point -> !isEmptyPoint(point)).collect(toList());
  }

  @JsonIgnore
  public Map<Long, List<Error>> getErrors() {
    return this.points.stream().collect(toMap(Point::getLineNo, Point::getErrors));
  }
}
