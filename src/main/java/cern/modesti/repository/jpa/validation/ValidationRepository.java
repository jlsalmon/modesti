/**
 *
 */
package cern.modesti.repository.jpa.validation;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Justin Lewis Salmon
 *
 */
@RepositoryRestResource(exported = false)
public interface ValidationRepository extends CrudRepository<ValidationResult, Long>, ValidationRepositoryCustom {

}
