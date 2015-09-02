package cern.modesti.request.point.rsql;

import cern.modesti.request.point.PointPredicateBuilder;
import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.LogicalOperator;
import cz.jirutka.rsql.parser.ast.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public class PointRsqlSpecBuilder {

  public BooleanExpression createSpecification(final Node node) {
    if (node instanceof LogicalNode) {
      return createSpecification((LogicalNode) node);
    }
    if (node instanceof ComparisonNode) {
      return createSpecification((ComparisonNode) node);
    }
    return null;
  }

  public BooleanExpression createSpecification(final LogicalNode logicalNode) {

    List<BooleanExpression> expressions = new ArrayList<>();
    BooleanExpression temp;

    for (final Node node : logicalNode.getChildren()) {
      temp = createSpecification(node);
      if (temp != null) {
        expressions.add(temp);
      }
    }

    BooleanExpression result = expressions.get(0);

    if (logicalNode.getOperator() == LogicalOperator.AND) {
      for (int i = 1; i < expressions.size(); i++) {
        result = result.and(expressions.get(i));
      }
    } else if (logicalNode.getOperator() == LogicalOperator.OR) {
      for (int i = 1; i < expressions.size(); i++) {
        result = result.or(expressions.get(i));
      }
    }

    return result;

//    final List<Specifications<Point>> specs = new ArrayList<>();
//    Specifications<Point> temp;
//    for (final Node node : logicalNode.getChildren()) {
//      temp = createSpecification(node);
//      if (temp != null) {
//        specs.add(temp);
//      }
//    }
//
//    Specifications<Point> result = specs.get(0);
//
//    if (logicalNode.getOperator() == LogicalOperator.AND) {
//      for (int i = 1; i < specs.size(); i++) {
//        result = Specifications.where(result).and(specs.get(i));
//      }
//    } else if (logicalNode.getOperator() == LogicalOperator.OR) {
//      for (int i = 1; i < specs.size(); i++) {
//        result = Specifications.where(result).or(specs.get(i));
//      }
//    }
//
//    return result;
  }

  public BooleanExpression createSpecification(final ComparisonNode comparisonNode) {

    return new PointPredicateBuilder().with(comparisonNode.getSelector(), comparisonNode.getOperator(), comparisonNode.getArguments()).build();

//    return Specifications.where(new PointRsqlSpecification(comparisonNode.getSelector(), comparisonNode.getOperator(), comparisonNode.getArguments()));
  }
}
