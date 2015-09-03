package cern.modesti.repository.point;

import cern.modesti.repository.base.ReadOnlyRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(exported = false)
public interface RefPointRepository extends ReadOnlyRepository<RefPoint, Long>/*, QueryDslPredicateExecutor<RefPoint>*/ {
}
