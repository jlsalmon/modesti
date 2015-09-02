package cern.modesti.request.point;

import cern.modesti.request.point.rsql.RsqlSearchOperation;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;
import lombok.AllArgsConstructor;

import java.util.List;

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
    PathBuilder<Point> entityPath = new PathBuilder<>(Point.class, "point");

    final List<String> args = criteria.getArguments();
    String argument = args.get(0);

    switch (RsqlSearchOperation.getSimpleOperator(criteria.getOperation())) {

      case EQUAL: {
        if (isNumeric(argument)) {
          NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
          int value = Integer.parseInt(argument);
          return path.eq(value);
          //return builder.like(root.<String>get(property), argument.toString().replace('*', '%'));
        }
//        else if (argument == null) {
//          return builder.isNull(root.get(property));
//        }
        else {
          StringPath path = entityPath.getString(criteria.getKey());
          return path.containsIgnoreCase(argument);
          //return builder.equal(root.get(property), argument);
        }
      }

      // TODO implement remaining operations

//      case NOT_EQUAL: {
//        if (argument instanceof String) {
//          return builder.notLike(root.<String>get(property), argument.toString().replace('*', '%'));
//        } else if (argument == null) {
//          return builder.isNotNull(root.get(property));
//        } else {
//          return builder.notEqual(root.get(property), argument);
//        }
//      }
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
      default: {
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
}