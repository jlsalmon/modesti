package cern.modesti.request;

import cern.modesti.point.Error;
import cern.modesti.point.Point;
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

  /**
   * Gets the persistence id in the database
   * @return Persistence id in the database
   */
  String getId();

  /**
   * Gets the request id
   * @return Request id
   */
  String getRequestId();

  /**
   * Gets the parent request id
   * @return Parent request id
   */
  String getParentRequestId();

  /**
   * Gets the child request ids
   * @return Child request ids
   */
  List<String> getChildRequestIds();

  /**
   * Gets the request status (IN_PROGRESS, FOR_APPROVAL, etc)
   * @return Request status
   */
  String getStatus();

  /**
   * Sets the request status (IN_PROGRESS, FOR_APPROVAL, etc)
   * @param status Request status
   */
  void setStatus(String status);

  /**
   * Gets the request type (CREATE, UPDATE, DELETE)
   * @return Request type
   */
  RequestType getType();

  /**
   * Sets the request type (CREATE, UPDATE, DELETE)
   * @param type Request type
   */
  void setType(RequestType type);

  /** 
   * Gets the request description
   * @return Request description
   */
  String getDescription();

  /**
   * Sets the request description
   * @param description Request description
   */
  void setDescription(String description);

  /**
   * Gets the request domain (TIM, CSAM, ...)
   * @return Request domain
   */
  String getDomain();

  /**
   * Sets the request domain (TIM, CSAM, ...)
   * @param domain Request domain
   */
  void setDomain(String domain);

  /**
   * Gets the request creator
   * @return Request creator
   */
  String getCreator();

  /**
   * Sets the request creator
   * @param username Request creator
   */
  void setCreator(String username);

  /**
   * Gets the assignee person
   * @return Assignee person
   */
  String getAssignee();

  /**
   * Sets the assignee person
   * @param username Assignee person
   */
  void setAssignee(String username);

  /**
   * Gets the request validity 
   * @return TRUE if and only if the request is valid
   */
  boolean isValid();

  /**
   * Sets the request validity
   * @param valid Request validity
   */
  void setValid(boolean valid);
  
  /**
   * Gets the valid of the skipCoreValidation flag. The core validations must be disabled
   * when UPDATE/DELETE requests provide the modified fields/point IDs.
   * @return TRUE if and only if the core validation must be skipped
   */
  @Deprecated
  boolean isSkipCoreValidation();
  
  /**
   * Sets the value for the skipCoreValidation flag. The core validations must be disabled
   * when UPDATE/DELETE requests provide the modified fields/point IDs.
   * @param skip Value for the skipCoreValidation flag
   */
  @Deprecated
  void setSkipCoreValidation(boolean skip);
  
  /**
   * Checks is the request is generated from the MODESTI UI, opposite than created from a REST request.
   * Requests generated from the UI must stop in the workflow stage IN_PROGRESS.
   * On the opposite, requests generated from the REST service already contain the necessary changes and
   * can continue in the workflow.
   * 
   * @return TRUE if and only if the request has been generated from the MODESTI UI
   */
  boolean isGeneratedFromUi();
  
  /**
   * Sets the value of the flag 'generatedFromUi'.
   * @param value Value of the flag 'generatedFromUi'
   */
  void setGeneratedFromUi(boolean value);

  /**
   * Gets the list of points in the request
   * @return List of points in the request
   */
  List<Point> getPoints();

  /**
   * Gets the list of non empty points in the request
   * @return List of non empty points in the request
   */
  List<Point> getNonEmptyPoints();

  /**
   * Sets the list of points for the request
   * @param points List of points for the request
   */
  void setPoints(List<Point> points);

  /**
   * Adds a new point to the request
   * @param point Point to add to the request
   */
  void addPoint(Point point);

  /**
   * Gets a map containing the request properties
   * @return map containing the request properties
   */
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

  /**
   * Sets a map containing the request properties
   * @param properties The request properties
   */
  void setProperties(Map<String, Object> properties);

  /**
   * Gets a map containing the list of errors for each line
   * @return Map containing the list of errors for each line
   */
  Map<Long, List<Error>> getErrors();
  
  /**
   * Set the list of errors in the request points.
   * 
   * @param errors Map where key=line number, value=list of errors
   */
  void setErrors(Map<Long, List<Error>> errors);

  /**
   * Gets the list of comments in the request
   * @return List of comments
   */
  List<Comment> getComments();

  /**
   * Gets the creation date of the request
   * @return Creation date of the request
   */
  DateTime getCreatedAt();

  /**
   * Gets the request version
   * @return Request version
   */
  Long getVersion();
}
