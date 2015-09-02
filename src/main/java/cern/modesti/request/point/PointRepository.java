package cern.modesti.request.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * @author Justin Lewis Salmon
 */
public interface PointRepository extends MongoRepository<Point, String>, QueryDslPredicateExecutor<Point> {

}
