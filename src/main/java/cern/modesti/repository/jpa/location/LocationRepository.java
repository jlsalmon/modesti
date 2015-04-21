/**
 * 
 */
package cern.modesti.repository.jpa.location;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface LocationRepository extends ReadOnlyRepository<Location, String> {

  @Query(value = "SELECT o.numero || '/' || l.etage || '-' || l.numloc AS location FROM local l, ouvrage o "
               + "WHERE  o.numero = l.numbat "
               + "AND    LOWER(o.numero || '/' || l.etage || '-' || l.numloc) LIKE LOWER(:q || '%') order by 1", nativeQuery = true)
  public List<Location> find(@Param("q") String q);
}
