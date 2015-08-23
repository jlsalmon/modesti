/**
 *
 */
package cern.modesti.request.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.webmvc.RepositoryRestController;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(exported = false)
public interface DomainRepository extends MongoRepository<Domain, String> {

  Domain findOneByNameIgnoreCase(@Param("name") String name);
}
