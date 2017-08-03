package cern.modesti.workflow.validation;

import cern.modesti.point.Point;
import cern.modesti.request.Request;
import cern.modesti.schema.category.Condition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public class Conditionals {

  private static ObjectMapper mapper = new ObjectMapper();

  public static boolean evaluate(Object conditional, Point point, Request request) {

    if (conditional instanceof Map) {
        Map conditionalMap = (Map)conditional;
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
    } else if (condition.getOperation().equals("in") && (value != null && ((List) condition.getValue()).contains(value.toString()))) {
      result = true;
    } else if (condition.getOperation().equals("notIn") && (value != null && !((List) condition.getValue()).contains(value.toString()))) {
      result = true;
    }

    return result;
  }
}
