package cern.modesti.point;

import cern.modesti.Application;
import cern.modesti.repository.person.Person;
import com.google.common.collect.ImmutableMap;
import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource("classpath:modesti-test.properties")
@WebIntegrationTest
@ActiveProfiles("test")
@Transactional
@TransactionConfiguration
public class PointPredicateTest {

  @Autowired
  private PointRepository repository;

  private Point point1;
  private Point point2;

  @Before
  public void init() {
    point1 = new Point();
    point1.setLineNo(1L);
    point1.setValid(true);
    point1.setProperties(ImmutableMap.of("pointDescription", "point1", "pointDatatype", "Boolean", "responsiblePerson", new Person(1L, "Bert", "bert")));
    repository.save(point1);

    point2 = new Point();
    point2.setLineNo(2L);
    point2.setValid(false);
    point2.setProperties(ImmutableMap.of("pointDescription", "point2", "pointDatatype", "Double", "responsiblePerson", new Person(2L, "Ernie", "ernie")));
    repository.save(point2);
  }

  @After
  public void tearDown() {
    repository.deleteAll();
  }

  @Test
  public void stringPropertyEquals() {
    Node rootNode = new RSQLParser().parse("thing==a");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);

    assertThat(results, contains(point1));
  }

  @Test
  public void numericPropertyEquals() {
    Node rootNode = new RSQLParser().parse("lineNo==2");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);

    assertThat(results, contains(point2));
  }

  @Test
  public void booleanPropertyEquals() {
    Node rootNode = new RSQLParser().parse("valid:false");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);
    assertThat(results, contains(point2));
  }

  @Test
  public void multiplePropertyEquals() {
    Node rootNode = new RSQLParser().parse("id==1;lineNo==1");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);
    assertThat(results, contains(point1));
  }

  @Test
  public void nonexistentProperty() {
    Node rootNode = new RSQLParser().parse("id==-1");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);
    assertThat(results, emptyIterable());
  }

  @Test
  public void nestedStringPropertyEquals() {
    Node rootNode = new RSQLParser().parse("properties.pointDescription == point1");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);
    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));

    rootNode = new RSQLParser().parse("properties.pointDescription == point2");
    predicate = rootNode.accept(new CustomRsqlVisitor());
    results = repository.findAll(predicate);
    assertThat(results, contains(point2));
    assertThat(results, not(contains(point1)));
  }

  @Test
  public void nestedMultiplePropertyEquals() {
    Node rootNode = new RSQLParser().parse("properties.pointDescription == point1 and properties.pointDatatype == Boolean");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);
    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));
  }

  @Test
  public void nestedObjectPropertyEquals() {
    Node rootNode = new RSQLParser().parse("properties.responsiblePerson.username == bert");
    BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    Iterable<Point> results = repository.findAll(predicate);
    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));
  }
}
