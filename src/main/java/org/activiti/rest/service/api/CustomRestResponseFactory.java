/**
 *
 */
package org.activiti.rest.service.api;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariableConverter;

import cern.modesti.request.Request;

/**
 * @author Justin Lewis Salmon
 *
 */
public class CustomRestResponseFactory extends RestResponseFactory {

  @Override
  protected void initializeVariableConverters() {
    // Add custom converter for MyPojo variables
    getVariableConverters().add(new MyPojoVariableConverter());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RestVariableConverter> getVariableConverters() {
    try {
      // Workaround until the "variableConverters" field is set protected on
      // RestResponseFactory or
      // the converters are exposed.
      Field f = RestResponseFactory.class.getDeclaredField("variableConverters");
      f.setAccessible(true);
      List<RestVariableConverter> variableConverters = (List<RestVariableConverter>) f.get(this);
      return variableConverters;

    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}

class MyPojoVariableConverter implements RestVariableConverter {

  @Override
  public String getRestTypeName() {
    return "request";
  }

  @Override
  public Class<?> getVariableType() {
    return Request.class;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getVariableValue(RestVariable result) {
    // Jackson deserializes all unknown JSON-structures as a map, containing
    // the properties,
    // which are potentially also Maps of properties
    Map<String, Object> jsonProperties = (Map<String, Object>) result.getValue();

    // Some validation on the given input, in real use-cases additional checks
    // are needed (eg. correct types)
    if (!jsonProperties.containsKey("name") || !jsonProperties.containsKey("email")) {
      // An ActivitiIllegalArgumentException will result in a 400 - BAD
      // REQUEST
      throw new ActivitiIllegalArgumentException("The request body should contain both name and email.");
    }

    return new Request();
  }

  @Override
  public void convertVariableValue(Object variableValue, RestVariable result) {
    // Let Jackson handle the serialisation of the POJO to JSON
    result.setValue(variableValue);
  }

}
