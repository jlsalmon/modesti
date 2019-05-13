package cern.modesti.workflow.validation;

import cern.modesti.point.Point;
import cern.modesti.request.Request;
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

  public static boolean evaluate(Object conditional, Point point, Request request) {

    if (conditional instanceof Map) {
        Map<String, Object> conditionalMap = (Map<String, Object>) conditional;
        if (conditionalMap.containsKey(request.getType().name())) {
            conditional = conditionalMap.get(request.getType().name());
        } else if (conditionalMap.containsKey("_")) {
          conditional = conditionalMap.get("_");
        }
    }

    if (conditional instanceof Boolean) {
      return (boolean) conditional;
    }

    List<Boolean> results = new ArrayList<>();
    Map map = (Map) conditional;

    // Chained OR condition
    if (map.containsKey("or")) {
      for (Object subConditional : (List) map.get("or")) {
        results.add(evaluate(subConditional, point, request));
      }

      return results.contains(true);
    } else if (map.containsKey("and")) {// Chained AND condition
      for (Object subConditional : (List) map.get("and")) {
        results.add(evaluate(subConditional, point, request));
      }

      return results.stream().reduce((a, b) -> (a == b) ? a : false).get();
    }

    Boolean statusResult = null;
    Boolean valueResult = null;

    // Conditional based on the status of the request.
    if (map.containsKey("status")) {
      Object conditionalStatus = map.get("status");

      if (conditionalStatus instanceof List) {
        statusResult = ((List) conditionalStatus).contains(request.getStatus());
      } else if (conditionalStatus instanceof String) {
        statusResult = conditionalStatus.equals(request.getStatus());
      }
    }

    // Conditional based on the value of another property of the point, used in conjunction with the status conditional
    if (map.containsKey("condition")) {
      valueResult = evaluate(map.get("condition"), point, request);
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

    switch (condition.getOperation()) {
    case "equals":
      result = value != null && value.equals(condition.getValue());
      break;
    case "notEquals":
      result = value == null || !value.equals(condition.getValue());
      break;
    case "contains":
      result = value != null && value.toString().contains(condition.getValue().toString());
      break;
    case "notNull":
      result = value != null && !value.toString().isEmpty();
      break;
    case "isNull":
      result = value == null || value.toString().isEmpty();
      break;
    case "in":
      result = value != null && ((List) condition.getValue()).contains(value.toString());
      break;
    case "notIn":
      result = value != null && !((List) condition.getValue()).contains(value.toString());
      break;
    default:
      result = false;
    }

    return result;
  }
}
