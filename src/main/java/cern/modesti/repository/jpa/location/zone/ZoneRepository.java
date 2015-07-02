/**
 *
 */
package cern.modesti.repository.jpa.location.zone;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface ZoneRepository extends ReadOnlyRepository<Zone, String> {

  @Query(value = "SELECT DISTINCT zone_secu as value FROM ouvrage "
               + "WHERE zone_secu LIKE :query || '%' "
               + "ORDER BY 1", nativeQuery = true)
  @Cacheable("zones")
  List<Zone> find(@Param("query") String query);
}
