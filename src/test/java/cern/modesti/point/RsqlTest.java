//package cern.modesti.point;
//
//import cern.modesti.Application;
//import cern.modesti.repository.person.Person;
//import cern.modesti.request.point.Point;
//import cern.modesti.request.point.PointRepository;
//import cern.modesti.request.point.rsql.CustomRsqlVisitor;
//import com.google.common.collect.ImmutableMap;
//import cz.jirutka.rsql.parser.RSQLParser;
//import cz.jirutka.rsql.parser.ast.Node;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.WebIntegrationTest;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.transaction.TransactionConfiguration;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.isIn;
//import static org.hamcrest.Matchers.not;
//
///**
// * @author Justin Lewis Salmon
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
//@TestPropertySource("classpath:modesti-test.properties")
//@WebIntegrationTest
//@ActiveProfiles("test")
//@Transactional
//@TransactionConfiguration
//public class RsqlTest {
//
//  @Autowired
//  private PointRepository repository;
//
//  private Point point1;
//  private Point point2;
//
//  @Before
//  public void init() {
//    point1 = new Point();
//    point1.setThing("a");
//    point1.setLineNo(1L);
//    point1.setValid(true);
//    point1.setProperties(ImmutableMap.of("pointDescription", "point1", "pointDatatype", "Boolean", "responsiblePerson", new Person(1L, "Bert", "bert")));
//    repository.save(point1);
//
//    point2 = new Point();
//    point2.setThing("b");
//    point2.setLineNo(2L);
//    point2.setValid(false);
//    point2.setProperties(ImmutableMap.of("pointDescription", "point2", "pointDatatype", "Double", "responsiblePerson", new Person(2L, "Ernie", "ernie")));
//    repository.save(point2);
//  }
//
//  @Test
//  public void givenFirstAndLastName_whenGettingListOfPoints_thenCorrect() {
//    final Node rootNode = new RSQLParser().parse("firstName==john;lastName==doe");
//    final Specification<Point> spec = rootNode.accept(new CustomRsqlVisitor<Point>());
//    final List<Point> results = repository.findAll(spec);
//
//    assertThat(PointJohn, isIn(results));
//    assertThat(PointTom, not(isIn(results)));
//  }
//
//  @Test
//  public void givenFirstNameInverse_whenGettingListOfPoints_thenCorrect() {
//    final Node rootNode = new RSQLParser().parse("firstName!=john");
//    final Specification<Point> spec = rootNode.accept(new CustomRsqlVisitor<Point>());
//    final List<Point> results = repository.findAll(spec);
//
//    assertThat(PointTom, isIn(results));
//    assertThat(PointJohn, not(isIn(results)));
//  }
//
//  @Test
//  public void givenMinAge_whenGettingListOfPoints_thenCorrect() {
//    final Node rootNode = new RSQLParser().parse("age>25");
//    final Specification<Point> spec = rootNode.accept(new CustomRsqlVisitor<Point>());
//    final List<Point> results = repository.findAll(spec);
//
//    assertThat(PointTom, isIn(results));
//    assertThat(PointJohn, not(isIn(results)));
//  }
//
//  @Test
//  public void givenFirstNamePrefix_whenGettingListOfPoints_thenCorrect() {
//    final Node rootNode = new RSQLParser().parse("firstName==jo*");
//    final Specification<Point> spec = rootNode.accept(new CustomRsqlVisitor<Point>());
//    final List<Point> results = repository.findAll(spec);
//
//    assertThat(PointJohn, isIn(results));
//    assertThat(PointTom, not(isIn(results)));
//  }
//
//  @Test
//  public void givenListOfFirstName_whenGettingListOfPoints_thenCorrect() {
//    final Node rootNode = new RSQLParser().parse("firstName=in=(john,jack)");
//    final Specification<Point> spec = rootNode.accept(new CustomRsqlVisitor<Point>());
//    final List<Point> results = repository.findAll(spec);
//
//    assertThat(PointJohn, isIn(results));
//    assertThat(PointTom, not(isIn(results)));
//  }
//}
