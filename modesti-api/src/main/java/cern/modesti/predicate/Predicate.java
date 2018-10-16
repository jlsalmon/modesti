package cern.modesti.predicate;

import com.google.common.base.CaseFormat;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import lombok.AllArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.util.json.JSONArray;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@AllArgsConstructor
public class Predicate<T> {

  private SearchCriteria criteria;

  private final Class<T> klass;

  public BooleanExpression getPredicate() {
    PathBuilder<T> entityPath = new PathBuilder<>(klass, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, klass.getSimpleName()));
    Field field = getField(criteria.getKey(), klass);

    final List<String> args = criteria.getArguments();
    String argument = args.get(0);

    boolean isString = field.getType().isAssignableFrom(String.class);
    boolean isNumeric = field.getType().isAssignableFrom(Float.class)
    		|| field.getType().isAssignableFrom(Integer.class)
    		|| field.getType().isAssignableFrom(Long.class);

    if (isNumeric) {
      return getNumericPredicate(entityPath, argument);
    } else if (isString) {
      return getStringPredicate(entityPath, argument);
    } else if (field.getType().isEnum()) {
      StringPath path = entityPath.getString(criteria.getKey());
      return path.eq(argument);
    } else {
      throw new InvalidPredicateException(format("Field type %s is not currently supported", field.getType()));
    }
  }

  private BooleanExpression getNumericPredicate(PathBuilder<T> entityPath, String argument) {
    
    if (RSQLOperators.IN.equals(criteria.getOperation())) {
      JSONArray array = new JSONArray(argument);
      List<Long> values = new ArrayList<>();
      for (int i=0; i< array.length(); i++) {
        values.add(array.optLong(i));
      }
      
      NumberPath<Long> path = entityPath.getNumber(criteria.getKey(), Long.class);
      return path.in(values);
    }
    
    NumberPath<Float> path = entityPath.getNumber(criteria.getKey(), Float.class);
    float value = Float.parseFloat(argument);

    if (RSQLOperators.EQUAL.equals(criteria.getOperation())) {
      return path.eq(value);
    } else if (RSQLOperators.NOT_EQUAL.equals(criteria.getOperation())) {
      return path.ne(value);
    } else if (RSQLOperators.GREATER_THAN.equals(criteria.getOperation())) {
      return path.gt(value);
    } else if (RSQLOperators.GREATER_THAN_OR_EQUAL.equals(criteria.getOperation())) {
      return path.goe(value);
    } else if (RSQLOperators.LESS_THAN.equals(criteria.getOperation())) {
      return path.lt(value);
    } else if (RSQLOperators.LESS_THAN_OR_EQUAL.equals(criteria.getOperation())) {
      return path.loe(value);
    } else {
      throw new InvalidPredicateException(String.format("Unknown operation %s", criteria.getOperation()));
    }
  }

  private BooleanExpression getStringPredicate(PathBuilder<T> entityPath, String argument) {
    StringPath path = entityPath.getString(criteria.getKey());
    BooleanExpression expression;

    if (RSQLOperators.IN.equals(criteria.getOperation())) {
      JSONArray array = new JSONArray(argument);
      List<String> values = new ArrayList<>();
      for (int i=0; i< array.length(); i++) {
        values.add(array.optString(i));
      }
      return path.in(values);
    }
    
    if (argument.startsWith("*") && argument.endsWith("*")) {
      expression = path.containsIgnoreCase(argument.substring(1, argument.length() - 1));
    } else if (argument.startsWith("*")) {
      expression = path.endsWithIgnoreCase(argument.substring(1, argument.length()));
    } else if (argument.endsWith("*")) {
      expression = path.startsWithIgnoreCase(argument.substring(0, argument.length() - 1));
    } else {
      expression = path.equalsIgnoreCase(argument);
    }

    if (RSQLOperators.EQUAL.equals(criteria.getOperation())) {
      return expression;
    } else {
      return expression.not();
    }
  }

  private Field getField(String fieldName, Class<T> klass) {
    Field field;
    String nestedFieldName = null;
    String name = fieldName;

    if (name.contains(".")) {
      String[] parts = name.split("\\.");
      name = parts[0];
      nestedFieldName = parts[1];
    }

    try {
      field = klass.getDeclaredField(name);

      if (nestedFieldName != null) {
        field = field.getType().getDeclaredField(nestedFieldName);
      }
    } catch (NoSuchFieldException e) {
      throw new InvalidPredicateException("Error creating predicate", e);
    }

    return field;
  }
}
