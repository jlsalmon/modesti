package cern.modesti.point;

import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class PointPredicateBuilder {
  private List<SearchCriteria> params;

  public PointPredicateBuilder() {
    params = new ArrayList<>();
  }

  public PointPredicateBuilder with(String key, ComparisonOperator operation, List<String> arguments) {
    params.add(new SearchCriteria(key, operation, arguments));
    return this;
  }

  public BooleanExpression build() {
    if (params.size() == 0) {
      return null;
    }

    List<BooleanExpression> predicates = new ArrayList<>();
    PointPredicate predicate;
    for (SearchCriteria param : params) {
      predicate = new PointPredicate(param);
      BooleanExpression exp = predicate.getPredicate();
      if (exp != null) {
        predicates.add(exp);
      }
    }

    BooleanExpression result = predicates.get(0);
    for (int i = 1; i < predicates.size(); i++) {
      result = result.and(predicates.get(i));
    }
    return result;
  }
}