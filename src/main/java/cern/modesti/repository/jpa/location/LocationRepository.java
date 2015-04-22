/**
 *
 */
package cern.modesti.repository.jpa.location;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cern.modesti.model.Location;
import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface LocationRepository extends ReadOnlyRepository<Location, String> {

  @Query(value = "SELECT o.numero || '/' || l.etage || '-' || l.numloc AS location FROM timref.local@timref_oper l, ouvrage o "
               + "WHERE  o.numero = l.numbat "
               + "AND    o.actif = 'Y'"
               + "AND    LOWER(o.numero || '/' || l.etage || '-' || l.numloc) LIKE LOWER(:q || '%') order by 1", nativeQuery = true)
  @Cacheable("locations")
  public List<Location> find(@Param("q") String q);
}
