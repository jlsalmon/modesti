package cern.modesti.point;

import cern.modesti.repository.point.RefPoint;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;
import lombok.AllArgsConstructor;

import java.util.List;

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@AllArgsConstructor
public class PointPredicate {

  private SearchCriteria criteria;

  public BooleanExpression getPredicate() {
    PathBuilder<RefPoint> entityPath = new PathBuilder<>(RefPoint.class, "refPoint");

    final List<String> args = criteria.getArguments();
    String argument = args.get(0);

    if (EQUAL.equals(criteria.getOperation())) {
      if (isNumeric(argument)) {
        NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
        int value = Integer.parseInt(argument);
        return path.eq(value);
      }
//        else if (argument == null) {
//
//        }
      else {
        StringPath path = entityPath.getString(criteria.getKey());
        //return path.containsIgnoreCase(argument);
        return path.containsIgnoreCase(argument);
      }
    }

    // TODO implement remaining operations
    else if (NOT_EQUAL.equals(criteria.getOperation())) {
      if (isNumeric(argument)) {
        NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
        int value = Integer.parseInt(argument);
        return path.ne(value);
//          return builder.notLike(root.<String>get(property), argument.toString().replace('*', '%'));
      }
//        else if (argument == null) {
//
//        }
      else {
        StringPath path = entityPath.getString(criteria.getKey());
        return path.notLike(argument);
      }
    }

    else if (GREATER_THAN.equals(criteria.getOperation())) {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    else if (GREATER_THAN_OR_EQUAL.equals(criteria.getOperation())) {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    else if (LESS_THAN.equals(criteria.getOperation())) {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    else if (LESS_THAN_OR_EQUAL.equals(criteria.getOperation())) {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    else if (IN.equals(criteria.getOperation())) {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    else if (NOT_IN.equals(criteria.getOperation())) {
      throw new UnsupportedOperationException("Not implemented yet");
    }


//      case GREATER_THAN: {
//        return builder.greaterThan(root.<String>get(property), argument.toString());
//      }
//      case GREATER_THAN_OR_EQUAL: {
//        return builder.greaterThanOrEqualTo(root.<String>get(property), argument.toString());
//      }
//      case LESS_THAN: {
//        return builder.lessThan(root.<String>get(property), argument.toString());
//      }
//      case LESS_THAN_OR_EQUAL: {
//        return builder.lessThanOrEqualTo(root.<String>get(property), argument.toString());
//      }
//      case IN: {
//        return root.get(property).in(args);
//      }
//      case NOT_IN: {
//        return builder.not(root.get(property).in(args));
//      }
    else {
      return null;
    }
  }

//    if (isNumeric(criteria.getValue().toString())) {
//      NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
//      int value = Integer.parseInt(criteria.getValue().toString());
//      if (criteria.getOperation().equalsIgnoreCase(":")) {
//        return path.eq(value);
//      }
//      else if (criteria.getOperation().equalsIgnoreCase(">")) {
//        return path.goe(value);
//      }
//      else if (criteria.getOperation().equalsIgnoreCase("<")) {
//        return path.loe(value);
//      }
//    }
//    else if ("true".equals(criteria.getValue().toString()) || "false".equals(criteria.getValue().toString())) {
//      BooleanPath path = entityPath.getBoolean(criteria.getKey());
//      if (criteria.getOperation().equalsIgnoreCase(":")) {
//        return path.eq(Boolean.parseBoolean(criteria.getValue().toString()));
//      }
//    }
//    else {
//      StringPath path = entityPath.getString(criteria.getKey());
//      if (criteria.getOperation().equalsIgnoreCase(":")) {
//        return path.containsIgnoreCase(criteria.getValue().toString());
//      }
//    }
//    return null;

}