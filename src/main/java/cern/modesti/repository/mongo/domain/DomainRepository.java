/**
 * 
 */
package cern.modesti.repository.mongo.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface DomainRepository extends MongoRepository<Domain, String> {

}
