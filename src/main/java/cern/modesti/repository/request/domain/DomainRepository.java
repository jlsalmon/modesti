/**
 * 
 */
package cern.modesti.repository.request.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface DomainRepository extends MongoRepository<Domain, String> {

}
