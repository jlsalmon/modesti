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

  @Query(value = "SELECT o.numero || '/' || l.etage || '-' || l.numloc AS location, o.numero AS buildingNumber, l.etage AS floor, l.numloc AS room " +
                 "FROM   timref.local@timref_oper l, ouvrage o  " +
                 "WHERE  o.numero = l.numbat  " +
                 "AND    o.actif = 'Y' " +
                 "AND   (LOWER(o.numero || '/' || l.etage || '-' || l.numloc) LIKE LOWER(:query || '%')  " +
                 "OR     LOWER(o.numero || ' ' || l.etage || '-' || l.numloc) LIKE LOWER(:query || '%'))  " +
                 "UNION  " +
                 "SELECT o.numero || '/' || l.etage AS location, o.numero AS buildingNumber, l.etage AS floor, '' AS room " +
                 "FROM   timref.local@timref_oper l, ouvrage o  " +
                 "WHERE  o.numero = l.numbat  " +
                 "AND    o.actif = 'Y'  " +
                 "AND   (LOWER(o.numero || '/' || l.etage) LIKE LOWER(:query || '%')  " +
                 "OR     LOWER(o.numero || ' ' || l.etage) LIKE LOWER(:query || '%'))  " +
                 "UNION  " +
                 "SELECT TO_CHAR(o.numero) AS location, o.numero AS buildingNumber, '' AS floor, '' AS room " +
                 "FROM   timref.local@timref_oper l, ouvrage o  " +
                 "WHERE  o.numero = l.numbat  " +
                 "AND    o.actif = 'Y'  " +
                 "AND    LOWER(o.numero) LIKE LOWER(:query || '%')  " +
                 "ORDER BY 1"
               , nativeQuery = true)
  @Cacheable("locations")
  List<Location> find(@Param("query") String query);


//  @Query(value = "SELECT DISTINCT '' AS location, '' as buildingNumber, s.sigle as buildingName, '' AS floor, '' AS room " +
//                 "FROM   timref.local@timref_oper l, ouvrage o, sigle s " +
//                 "WHERE  o.numero = l.numbat " +
//                 "AND    o.numero = s.numero " +
//                 "AND    o.actif = 'Y' " +
//                 "AND    o.numero LIKE LOWER(:buildingNumber || '%') " +
//                 "AND    s.sigle  LIKE UPPER(:q || '%') " +
//                 "ORDER BY 1"
//                 , nativeQuery = true)
//  List<Location> findByBuildingNumber(@Param("q") String q, @Param("buildingNumber") String buildingNumber);
}
