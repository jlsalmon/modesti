package cern.modesti.request.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Created by jussy on 31/08/15.
 */
public interface PointRepository extends MongoRepository<Point, String>, QueryDslPredicateExecutor<Point> {

  Page<Point> findByValid(@RequestParam("valid") Boolean valid, Pageable pageable);
}
