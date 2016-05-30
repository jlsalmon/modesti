package cern.modesti.workflow.validation;

import cern.modesti.request.point.Point;
import cern.modesti.schema.category.Condition;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 */
public class Conditionals {

  private static ObjectMapper mapper = new ObjectMapper();

  public static boolean evaluate(Object conditional, Point point, String requestStatus) {

    // Simple boolean
    if (conditional instanceof Boolean) {
      return (boolean) conditional;
    }

    List<Boolean> results = new ArrayList<>();
    Map map = (Map) conditional;

    // Chained OR condition
    if (map.containsKey("or")) {
      for (Object subConditional : (List) map.get("or")) {
        results.add(evaluate(subConditional, point, requestStatus));
      }

      return results.contains(true);
    }

    // Chained AND condition
    else if (map.containsKey("and")) {
      for (Object subConditional : (List) map.get("and")) {
        results.add(evaluate(subConditional, point, requestStatus));
      }

      return results.stream().reduce((a, b) -> (a == b) ? a : false).get();
    }

    Boolean statusResult = null, valueResult = null;

    // Conditional based on the status of the request.
    if (map.containsKey("status")) {
      Object conditionalStatus = map.get("status");

      if (conditionalStatus instanceof List) {
        statusResult = ((List) conditionalStatus).contains(requestStatus);
      } else if (conditionalStatus instanceof String) {
        statusResult = conditionalStatus.equals(requestStatus);
      }
    }

    // Conditional based on the value of another property of the point, used in conjunction with the status conditional
    if (map.containsKey("condition")) {
      valueResult = evaluate(map.get("condition"), point, requestStatus);
    }

    // Simple value conditional without status conditional
    if (map.containsKey("field")) {
      valueResult = evaluateValueCondition(mapper.convertValue(map, Condition.class), point);
    }

    if (valueResult != null && statusResult != null) {
      return statusResult && valueResult;
    } else if (valueResult == null && statusResult != null) {
      return statusResult;
    } else if (valueResult != null) {
      return valueResult;
    } else {
      return false;
    }
  }


  private static boolean evaluateValueCondition(Condition condition, Point point) {
    Object value = point.getProperties().get(condition.getField());
    boolean result = false;

    if (condition.getOperation().equals("equals") && (value != null && value.equals(condition.getValue()))) {
      result = true;
    } else if (condition.getOperation().equals("notEquals") && (value == null || !value.equals(condition.getValue()))) {
      result = true;
    } else if (condition.getOperation().equals("contains") && (value != null && value.toString().contains(condition.getValue().toString()))) {
      result = true;
    } else if (condition.getOperation().equals("notNull") && (value != null && !value.toString().isEmpty())) {
      result = true;
    } else if (condition.getOperation().equals("isNull") && (value == null || value.toString().isEmpty())) {
      result = true;
    } else if (condition.getOperation().equals("in") && (value != null && ((List)condition.getValue()).contains(value.toString()))) {
      result = true;
    }

    return result;
  }
}
