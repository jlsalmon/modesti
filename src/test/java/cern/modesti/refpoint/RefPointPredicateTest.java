//package cern.modesti.refpoint;
//
//import cern.modesti.repository.refpoint.TimPoint;
//import cern.modesti.repository.refpoint.TimPointRepository;
//import cern.modesti.request.search.RsqlExpressionBuilder;
//import cern.modesti.test.BaseIntegrationTest;
//import com.mysema.query.types.Predicate;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import javax.transaction.Transactional;
//
//import static org.hamcrest.Matchers.*;
//import static org.junit.Assert.assertThat;
//
///**
// * TODO
// *
// * @author Justin Lewis Salmon
// */
//@Transactional
//public class RefPointPredicateTest extends BaseIntegrationTest {
//
//  @Autowired
//  private TimPointRepository repository;
//
//  private TimPoint point1;
//  private TimPoint point2;
//
//  @Before
//  public void init() {
//    point1 = new TimPoint();
//    point1.setPointId(1L);
//    point1.setPointDescription("point 1");
//    point1.setPointDatatype("Boolean");
//    point1.setBuildingNumber(104L);
//    repository.save(point1);
//
//    point2 = new TimPoint();
//    point2.setPointId(2L);
//    point2.setPointDescription("point2");
//    point2.setPointDatatype("Double");
//    point2.setBuildingNumber(874L);
//    repository.save(point2);
//  }
//
//  @After
//  public void tearDown() {
//    repository.deleteAll();
//  }
//
//  @Test
//  public void stringPropertyEquals() {
//    Predicate predicate = new RsqlExpressionBuilder<>(TimPoint.class).createExpression("pointDescription == 'point 1'");
//    Iterable<TimPoint> results = repository.findAll(predicate);
//
//    assertThat(results, contains(point1));
//    assertThat(results, not(contains(point2)));
//  }
//
//  @Test
//  public void stringPropertyNotEquals() {
//    Predicate predicate = new RsqlExpressionBuilder<>(TimPoint.class).createExpression("pointDescription != 'point 1'");
//    Iterable<TimPoint> results = repository.findAll(predicate);
//
//    assertThat(results, not(contains(point1)));
//    assertThat(results, contains(point2));
//  }
//
//  @Test
//  public void numericPropertyEquals() {
//    Predicate predicate = new RsqlExpressionBuilder<>(TimPoint.class).createExpression("id == 2");
//    Iterable<TimPoint> results = repository.findAll(predicate);
//
//    assertThat(results, not(contains(point1)));
//    assertThat(results, contains(point2));
//  }
//
//  @Test
//  public void numericPropertyNotEquals() {
//    Predicate predicate = new RsqlExpressionBuilder<>(TimPoint.class).createExpression("id != 2");
//    Iterable<TimPoint> results = repository.findAll(predicate);
//
//    assertThat(results, contains(point1));
//    assertThat(results, not(contains(point2)));
//  }
//
//  @Test
//  public void multiplePropertyEquals() {
//    Predicate predicate = new RsqlExpressionBuilder<>(TimPoint.class).createExpression("id == 1 and pointDatatype == Boolean");
//    Iterable<TimPoint> results = repository.findAll(predicate);
//
//    assertThat(results, contains(point1));
//    assertThat(results, not(contains(point2)));
//  }
//
//  @Test
//  public void nonexistentProperty() {
//    Predicate predicate = new RsqlExpressionBuilder<>(TimPoint.class).createExpression("id == 999");
//    Iterable<TimPoint> results = repository.findAll(predicate);
//
//    assertThat(results, emptyIterable());
//  }
//}
