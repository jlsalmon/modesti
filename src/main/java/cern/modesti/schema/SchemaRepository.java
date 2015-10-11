package cern.modesti.schema;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface SchemaRepository extends MongoRepository<Schema, String> {

  @Override
  @PreAuthorize("hasRole('modesti-administrators')")
  Schema save(Schema schema);
}
