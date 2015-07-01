/**
 *
 */
package cern.modesti.repository.jpa.validation;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 *
 */
@RepositoryRestResource(exported = false)
public interface ValidationRepository extends CrudRepository<DraftPoint, Long>, ValidationRepositoryCustom {

  List<DraftPoint> findByRequestId(@Param("requestId") Long requestId);
}
