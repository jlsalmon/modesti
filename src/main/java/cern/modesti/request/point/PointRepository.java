package cern.modesti.request.point;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by jussy on 31/08/15.
 */
public interface PointRepository extends MongoRepository<Point, Long> {

}
