package cern.modesti.refpoint;

import cern.modesti.repository.refpoint.RefPoint;
import cern.modesti.repository.refpoint.RefPointRepository;
import cern.modesti.util.BaseIntegrationTest;
import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Transactional
@TransactionConfiguration
public class RefPointPredicateTest extends BaseIntegrationTest {

  @Autowired
  private RefPointRepository repository;

  private RefPoint point1;
  private RefPoint point2;

  @Before
  public void init() {
    point1 = new RefPoint();
    point1.setPointId(1L);
    point1.setPointDescription("point 1");
    point1.setPointDatatype("Boolean");
    point1.setBuildingNumber(104L);
    repository.save(point1);

    point2 = new RefPoint();
    point2.setPointId(2L);
    point2.setPointDescription("point2");
    point2.setPointDatatype("Double");
    point2.setBuildingNumber(874L);
    repository.save(point2);
  }

  @After
  public void tearDown() {
    repository.deleteAll();
  }

  @Test
  public void stringPropertyEquals() {
    Node rootNode = new RSQLParser().parse("pointDescription == 'point 1'");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<RefPoint> results = repository.findAll(predicate);

    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));
  }

  @Test
  public void stringPropertyNotEquals() {
    Node rootNode = new RSQLParser().parse("pointDescription != 'point 1'");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<RefPoint> results = repository.findAll(predicate);

    assertThat(results, not(contains(point1)));
    assertThat(results, contains(point2));
  }

  @Test
  public void numericPropertyEquals() {
    Node rootNode = new RSQLParser().parse("id == 2");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<RefPoint> results = repository.findAll(predicate);

    assertThat(results, not(contains(point1)));
    assertThat(results, contains(point2));
  }

  @Test
  public void numericPropertyNotEquals() {
    Node rootNode = new RSQLParser().parse("id != 2");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<RefPoint> results = repository.findAll(predicate);

    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));
  }

  @Test
  public void multiplePropertyEquals() {
    Node rootNode = new RSQLParser().parse("id == 1 and pointDatatype == Boolean");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<RefPoint> results = repository.findAll(predicate);

    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));
  }

  @Test
  public void nonexistentProperty() {
    Node rootNode = new RSQLParser().parse("id == 999");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<RefPoint> results = repository.findAll(predicate);

    assertThat(results, emptyIterable());
  }
}
