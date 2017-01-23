package cern.modesti.request;

import cern.modesti.point.Error;
import cern.modesti.point.Point;
import cern.modesti.user.User;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
public interface Request extends Serializable {

  String getId();

  String getRequestId();

  String getParentRequestId();

  List<String> getChildRequestIds();

  String getStatus();

  void setStatus(String status);

  RequestType getType();

  void setType(RequestType type);

  String getDescription();

  void setDescription(String description);

  String getDomain();

  void setDomain(String domain);

  String getCreator();

  void setCreator(String username);

  String getAssignee();

  void setAssignee(String username);

  boolean isValid();

  void setValid(boolean valid);

  List<Point> getPoints();

  List<Point> getNonEmptyPoints();

  void setPoints(List<Point> points);

  void addPoint(Point point);

  Map<String, Object> getProperties();

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
  <T> T getProperty(String key, Class<T> klass);

  void setProperties(Map<String, Object> properties);

  Map<Long, List<Error>> getErrors();

  List<Comment> getComments();

  DateTime getCreatedAt();

  Long getVersion();
}
