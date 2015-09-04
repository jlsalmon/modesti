package cern.modesti.repository.refpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(exported = false)
public interface RefPointRepository extends JpaRepository<RefPoint, Long>, QueryDslPredicateExecutor<RefPoint> {
}
