/**
 *
 */
package cern.modesti.repository.location;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface LocationRepository extends ReadOnlyRepository<Location, String> {

  /**
   * Allows results such as:
   *
   * 104
   * 104/R
   * 104 R
   * 104/R-A01
   * 104 R-A01
   *
   * @param query
   *
   * @return
   */
  @Query(value = "" +
      "SELECT o.numero || '/' || l.etage || '-' || l.numloc AS value,                          " +
      "       o.numero AS buildingNumber,                                                      " +
      "       l.etage  AS floor,                                                               " +
      "       l.numloc AS room                                                                 " +
      "FROM   timref.local@timref_oper l,                                                      " +
      "       ouvrage o                                                                        " +
      "WHERE  o.numero = l.numbat                                                              " +
      "AND    o.actif = 'Y'                                                                    " +
      "AND   (LOWER(o.numero || '/' || l.etage || '-' || l.numloc) LIKE LOWER(:query || '%')   " +
      "OR     LOWER(o.numero || ' ' || l.etage || '-' || l.numloc) LIKE LOWER(:query || '%'))  " +

      "UNION                                                                                   " +

      "SELECT o.numero || '/' || l.etage AS value,                                             " +
      "       o.numero AS buildingNumber,                                                      " +
      "       l.etage  AS floor,                                                               " +
      "       ''       AS room                                                                 " +
      "FROM   timref.local@timref_oper l,                                                      " +
      "       ouvrage o                                                                        " +
      "WHERE  o.numero = l.numbat                                                              " +
      "AND    o.actif = 'Y'                                                                    " +
      "AND   (LOWER(o.numero || '/' || l.etage) LIKE LOWER(:query || '%')                      " +
      "OR     LOWER(o.numero || ' ' || l.etage) LIKE LOWER(:query || '%'))                     " +

      "UNION                                                                                   " +

      "SELECT TO_CHAR(o.numero) AS value,                                                      " +
      "       o.numero AS buildingNumber,                                                      " +
      "       '' AS floor,                                                                     " +
      "       '' AS room                                                                       " +
      "FROM   timref.local@timref_oper l,                                                      " +
      "       ouvrage o                                                                        " +
      "WHERE  o.numero = l.numbat                                                              " +
      "AND    o.actif = 'Y'                                                                    " +
      "AND    LOWER(o.numero) LIKE LOWER(:query || '%')                                        " +
      "ORDER BY 1", nativeQuery = true)

  @Cacheable("locations")
  List<Location> find(@Param("query") String query);

//  @Query(value = "" +
//      "SELECT ", nativeQuery = true)
//  Location find(@Param("buildingNumber") String buildingNumber, @Param("buildingFloor") String buildingFloor, @Param("buildingRoom") String buildingRoom);
}
