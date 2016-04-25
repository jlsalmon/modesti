package cern.modesti.predicate;

import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.*;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating {@link Predicate} instances from RSQL query
 * strings.
 *
 * @author Justin Lewis Salmon
 */
@AllArgsConstructor
public class RsqlExpressionBuilder<T> implements RSQLVisitor<BooleanExpression, Void> {

  private final Class<T> klass;

  /**
   * Create a {@link Predicate} instance from the given RSQL query string.
   *
   * @param search the RSQL query string
   * @return a {@link Predicate} instance built from the given query
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

  private BooleanExpression createExpression(final ComparisonNode comparisonNode) {
    return new PredicateBuilder<>(klass).with(comparisonNode.getSelector(), comparisonNode.getOperator(), comparisonNode.getArguments()).build();
  }

  private BooleanExpression createExpression(final Node node) {
    if (node instanceof LogicalNode) {
      return createExpression((LogicalNode) node);
    }
    if (node instanceof ComparisonNode) {
      return createExpression((ComparisonNode) node);
    }
    return null;
  }

  private BooleanExpression createExpression(final LogicalNode logicalNode) {
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
