package cern.modesti.point;

import cern.modesti.request.Request;
import cern.modesti.schema.field.Field;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

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

  Long getLineNo();

  void setLineNo(Long lineNo);

  boolean isDirty();

  boolean isSelected();

  boolean isValid();

  void setValid(Boolean valid);

  List<Error> getErrors();

  void setErrors(List<Error> errors);

  Map<String, Object> getProperties();

  void setProperties(Map<String, Object> properties);

  <T> T getProperty(String key, Class<T> klass);

  <T> T getProperty(String key, Class<T> klass, T defaultValue);

  Object getValueByPropertyName(String propertyName);

  boolean isEmpty();

  void addProperty(String key, Object value);

  void addErrorMessage(String category, String property, String message);

  List<Field> getEmptyFields(List<Field> fields);
}
