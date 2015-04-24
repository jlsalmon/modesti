/**
 *
 */
package cern.modesti.repository.mongo.validation;

import org.springframework.data.mongodb.repository.MongoRepository;

import cern.modesti.repository.jpa.validation.ValidationResult;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface ValidationResultRepository extends MongoRepository<ValidationResult, String> {

}
