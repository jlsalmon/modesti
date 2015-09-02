package cern.modesti.point;

import cern.modesti.Application;
import cern.modesti.repository.person.Person;
import cern.modesti.repository.subsystem.SubSystem;
import cern.modesti.request.point.Point;
import cern.modesti.request.point.PointPredicateBuilder;
import cern.modesti.request.point.PointRepository;
import com.google.common.collect.ImmutableMap;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.transaction.Transactional;

import java.util.HashMap;

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
  private PointRepository repo;

  private Point point1;
  private Point point2;

  @Before
  public void init() {
    point1 = new Point();
    point1.setThing("a");
    point1.setLineNo(1L);
    point1.setValid(true);
    point1.setProperties(ImmutableMap.of("pointDescription", "point1", "pointDatatype", "Boolean", "responsiblePerson", new Person(1L, "Bert", "bert")));
    repo.save(point1);

    point2 = new Point();
    point2.setThing("b");
    point2.setLineNo(2L);
    point2.setValid(false);
    point2.setProperties(ImmutableMap.of("pointDescription", "point2", "pointDatatype", "Double", "responsiblePerson", new Person(2L, "Ernie", "ernie")));
    repo.save(point2);
  }

  @After
  public void tearDown() {
    repo.deleteAll();
  }

  @Test
  public void stringPropertyEquals() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("thing", ":", "a");

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, contains(point1));
  }

  @Test
  public void numericPropertyEquals() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("lineNo", ":", 2);

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, contains(point2));
  }

  @Test
  public void booleanPropertyEquals() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("valid", ":", false);

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, contains(point2));
  }

  @Test
  public void multiplePropertyEquals() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("id", ":", "1").with("lineNo", ":", 1);

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, contains(point1));
  }

  @Test
  public void nonexistentProperty() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("id", ":", "-1");

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, emptyIterable());
  }

  @Test
  public void nestedStringPropertyEquals() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("properties.pointDescription", ":", "point1");

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));

    builder = new PointPredicateBuilder().with("properties.pointDescription", ":", "point2");
    results = repo.findAll(builder.build());
    assertThat(results, contains(point2));
    assertThat(results, not(contains(point1)));
  }

  @Test
  public void nestedMultiplePropertyEquals() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("properties.pointDescription", ":", "point1").with("properties.pointDatatype", ":", "Boolean");

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));
  }

  @Test
  public void nestedObjectPropertyEquals() {
    PointPredicateBuilder builder = new PointPredicateBuilder().with("properties.responsiblePerson.username", ":", "bert");

    Iterable<Point> results = repo.findAll(builder.build());
    assertThat(results, contains(point1));
    assertThat(results, not(contains(point2)));
  }
}
