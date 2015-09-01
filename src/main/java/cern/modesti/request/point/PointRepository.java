package cern.modesti.request.point;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import sun.tools.tree.StringExpression;

/**
 * Created by jussy on 31/08/15.
 */
public interface PointRepository extends MongoRepository<Point, String>, QueryDslPredicateExecutor<Point> {


}
