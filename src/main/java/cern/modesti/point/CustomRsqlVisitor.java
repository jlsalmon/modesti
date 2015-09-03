package cern.modesti.point;

import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

/**
 * @author Justin Lewis Salmon
 */
public class CustomRsqlVisitor implements RSQLVisitor<BooleanExpression, Void> {

  private PointRsqlSpecBuilder builder;

  public CustomRsqlVisitor() {
    builder = new PointRsqlSpecBuilder();
  }

  @Override
  public BooleanExpression visit(AndNode node, Void param) {
    return builder.createSpecification(node);
  }

  @Override
  public BooleanExpression visit(OrNode node, Void param) {
    return builder.createSpecification(node);
  }

  @Override
  public BooleanExpression visit(ComparisonNode node, Void params) {
    return builder.createSpecification(node);
  }
}