/**
 *
 */
package cern.modesti.repository.request.datasource;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface DatasourceRepository extends MongoRepository<Datasource, String> {

}
