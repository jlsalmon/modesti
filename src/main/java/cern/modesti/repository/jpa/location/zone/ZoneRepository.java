/**
 *
 */
package cern.modesti.repository.jpa.location.zone;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cern.modesti.model.Zone;
import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface ZoneRepository extends ReadOnlyRepository<Zone, String> {

  @Query(value = "SELECT DISTINCT zone_secu as name FROM ouvrage "
               + "WHERE zone_secu LIKE :name || '%' "
               + "ORDER BY 1", nativeQuery = true)
  @Cacheable("zones")
  public List<Zone> findByName(@Param("name") String name);
}
