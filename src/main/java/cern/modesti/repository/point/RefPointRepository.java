package cern.modesti.repository.point;

import cern.modesti.repository.base.ReadOnlyRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @author Justin Lewis Salmon
 */

public interface RefPointRepository extends ReadOnlyRepository<RefPoint, Long>, QueryDslPredicateExecutor<RefPoint> {
}
