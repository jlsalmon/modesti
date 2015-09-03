package cern.modesti.point;

import cern.modesti.point.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


/**
 * @author Justin Lewis Salmon
 */
public interface PointRepository extends MongoRepository<Point, String>, QueryDslPredicateExecutor<Point> {

}
