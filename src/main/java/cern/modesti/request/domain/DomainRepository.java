/**
 *
 */
package cern.modesti.request.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Justin Lewis Salmon
 */
public interface DomainRepository extends MongoRepository<Domain, String> {

  Domain findOneByNameIgnoreCase(@Param("name") String name);
}
