package cern.modesti.point;

import cern.modesti.request.Request;
import cern.modesti.schema.field.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class represents a single data point. A
 * {@link Request}. is composed of multiple points.
 * <p>
 * A point holds a map of arbitrary properties. These properties can be either
 * primitive values or complex objects.
 * <p>
 * In the case of complex object properties, the
 * {@link #getProperty(String, Class)} utility method can be used to
 * retrieve them as their specific domain class instance.
 * <p>
 * For example:
 * <p>
 * <code>
 * Point point = new Point();
 * point.addProperty("myDomainObject", new MyDomainObject());
 * MyDomainObject myDomainObject = point.getProperty("myDomainObject", MyDomainObject.class);
 * </code>
 *
 * @author Justin Lewis Salmon
 */
public interface Point extends Serializable {

  /**
   * Gets the line number of the point in the request
   * @return Line number
   */
  Long getLineNo();

  /**
   * Sets the line number of the point in the request
   * @param lineNo line number
   */
  void setLineNo(Long lineNo);

  /**
   * Checks if the point is marked as dirty (modified and not saved/rendered)
   * @return TRUE if and only if the point is marked as dirty
   */
  boolean isDirty();

  /**
   * Checks if the point is selected in the front-end
   * @return TRUE if and only if the point is selected
   */
  boolean isSelected();

  /**
   * Checks if the point has no validation errors
   * @return TRUE if and only if the point has no validation errors
   */
  boolean isValid();

  /**
   * Sets the valid state of the point
   * @param valid Valid state of the point
   */
  void setValid(Boolean valid);

  /**
   * Gets the list of validation errors for the point
   * @return List of validation errors for the point
   */
  List<Error> getErrors();

  /**
   * Sets the list of validation erros for the point
   * @param errors List of validation errors
   */
  void setErrors(List<Error> errors);

  /**
   * Gets a map containing the point properties
   * @return Map containing the point properties
   */
  Map<String, Object> getProperties();

  /**
   * Sets the point properties
   * @param properties Map containing the point properties
   */
  void setProperties(Map<String, Object> properties);

  /**
   * Retrieve a point property and convert it to the given type.
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
   * Retrieve a point property in the same way as
   * {@link #getProperty(String, Class)}, except that the provided default
   * value will be returned in case that the property value does not exist or
   * is null.
   *
   * @param key          the property key
   * @param klass        the type to which to convert the value
   * @param defaultValue the default value to return
   * @param <T>          he type of the value
   * @return the value mapped by the given key, converted to the given type or
   *         the provided default value
   */
  <T> T getProperty(String key, Class<T> klass, T defaultValue);

  /**
   * Gets a point property by name
   * @param propertyName Property name
   * @return Property value
   */
  Object getValueByPropertyName(String propertyName);

  /**
   * Check to see if this point is empty. A point is considered empty
   * if all the values of its properties are either null or empty string.
   *
   * @return true if the point was considered empty, false otherwise
   */
  boolean isEmpty();

  /**
   * Adds a new point property
   * @param key Property name
   * @param value Property value
   */
  void addProperty(String key, Object value);

  /**
   * Add an error message associated with a property of the point.
   * <p>
   * This method is designed to be used as a means of displaying validation
   * errors or other types of errors visually. The errors can be associated
   * directly with individual properties of the point.
   *
   * @param category the category to which the property belongs
   * @param property the property (map key) to associate the error with
   * @param message  the error message
   */
  void addErrorMessage(String category, String property, String message);

  /**
   * Gets the list of fields with empty values from the received parameter
   * @param fields List of fields to verify
   * @return List of empty fields from the received argument
   */
  List<Field> getEmptyFields(List<Field> fields);
}
