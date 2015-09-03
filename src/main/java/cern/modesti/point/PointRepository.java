package cern.modesti.point;

import cern.modesti.point.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(exported = false)
public interface PointRepository extends MongoRepository<Point, Long>, QueryDslPredicateExecutor<Point> {

}
