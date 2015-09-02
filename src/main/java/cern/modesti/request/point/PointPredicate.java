package cern.modesti.request.point;

import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;
import lombok.AllArgsConstructor;

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

    if (isNumeric(criteria.getValue().toString())) {
      NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
      int value = Integer.parseInt(criteria.getValue().toString());
      if (criteria.getOperation().equalsIgnoreCase(":")) {
        return path.eq(value);
      }
      else if (criteria.getOperation().equalsIgnoreCase(">")) {
        return path.goe(value);
      }
      else if (criteria.getOperation().equalsIgnoreCase("<")) {
        return path.loe(value);
      }
    }
    else if ("true".equals(criteria.getValue().toString()) || "false".equals(criteria.getValue().toString())) {
      BooleanPath path = entityPath.getBoolean(criteria.getKey());
      if (criteria.getOperation().equalsIgnoreCase(":")) {
        return path.eq(Boolean.parseBoolean(criteria.getValue().toString()));
      }
    }
    else {
      StringPath path = entityPath.getString(criteria.getKey());
      if (criteria.getOperation().equalsIgnoreCase(":")) {
        return path.containsIgnoreCase(criteria.getValue().toString());
      }
    }
    return null;
  }
}