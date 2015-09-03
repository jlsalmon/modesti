package cern.modesti.point;

import cern.modesti.repository.point.RefPoint;
import cern.modesti.repository.point.RefPointRepository;
import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class PointController {

  @Autowired
  private RefPointRepository repository;

//  @RequestMapping(value = "/points", method = GET, produces = "application/json")
//  ResponseEntity<Resources<Point>> getPoints(@RequestParam("search") String search) {
//
//    PointPredicateBuilder builder = new PointPredicateBuilder();
//    Pattern pattern = Pattern.compile("([\\w.]+?)(:|<|>)(\\w.+?),");
//    Matcher matcher = pattern.matcher(search + ",");
//    while (matcher.find()) {
//      builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
//    }
//
//    BooleanExpression predicate = builder.build();
//    if (predicate == null) {
//      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//    }
//
//
//    Iterable<Point> points = repository.findAll(predicate);
//
//
//    Resources<Point> resources = new Resources<>(points);
//    return new ResponseEntity<>(resources, HttpStatus.OK);
//  }
//
//  @RequestMapping(value = "/points2", method = GET, produces = "application/json")
//  ResponseEntity<Resources<Point>> getPoints(@QuerydslPredicate(root = Point.class) Predicate predicate) {
//
//    Iterable<Point> points = repository.findAll(predicate);
//
//    Resources<Point> resources = new Resources<>(points);
//    return new ResponseEntity<>(resources, HttpStatus.OK);
//  }

  @RequestMapping(value = "/points", method = GET, produces = "application/json")
  ResponseEntity<Resources<RefPoint>> getPointsByRsql(@RequestParam("search") String search) {

    final Node rootNode = new RSQLParser().parse(search);
    final BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    log.debug(predicate.toString());

    Iterable<RefPoint> points = repository.findAll(predicate);

    Resources<RefPoint> resources = new Resources<>(points);
    return new ResponseEntity<>(resources, HttpStatus.OK);
  }
}