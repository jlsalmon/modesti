package cern.modesti.predicate;

import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.*;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@AllArgsConstructor
public class RsqlExpressionBuilder<T> implements RSQLVisitor<BooleanExpression, Void> {

  private final Class<T> klass;

  /**
   *
   * @param search
   * @return
   */
  public Predicate createExpression(String search) {
    final Node rootNode = new RSQLParser().parse(search);
    return rootNode.accept(this);
  }

  @Override
  public BooleanExpression visit(AndNode node, Void param) {
    return createExpression(node);
  }

  @Override
  public BooleanExpression visit(OrNode node, Void param) {
    return createExpression(node);
  }

  @Override
  public BooleanExpression visit(ComparisonNode node, Void params) {
    return createExpression(node);
  }

  public BooleanExpression createExpression(final ComparisonNode comparisonNode) {
    return new PredicateBuilder<>(klass).with(comparisonNode.getSelector(), comparisonNode.getOperator(), comparisonNode.getArguments()).build();
  }

  /**
   *
   * @param node
   * @return
   */
  public BooleanExpression createExpression(final Node node) {
    if (node instanceof LogicalNode) {
      return createExpression((LogicalNode) node);
    }
    if (node instanceof ComparisonNode) {
      return createExpression((ComparisonNode) node);
    }
    return null;
  }

  /**
   *
   * @param logicalNode
   * @return
   */
  public BooleanExpression createExpression(final LogicalNode logicalNode) {

    List<BooleanExpression> expressions = new ArrayList<>();
    BooleanExpression temp;

    for (final Node node : logicalNode.getChildren()) {
      temp = createExpression(node);
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
  }
}
