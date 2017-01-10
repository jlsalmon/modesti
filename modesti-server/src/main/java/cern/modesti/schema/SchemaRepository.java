package cern.modesti.schema;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Repository for creating/updating/deleting/searching {@link Schema}
 * instances.
 *
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(path = "schemas", collectionResourceRel = "schemas", itemResourceRel = "schema")
public interface SchemaRepository extends MongoRepository<SchemaImpl, String> {

  @Override
  @PreAuthorize("hasRole('modesti-administrators')")
  SchemaImpl save(SchemaImpl schema);
}
