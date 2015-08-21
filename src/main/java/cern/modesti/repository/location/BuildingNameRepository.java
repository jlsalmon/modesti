package cern.modesti.repository.location;

import cern.modesti.repository.base.ReadOnlyRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface BuildingNameRepository extends ReadOnlyRepository<BuildingName, String> {

  @Query(value = "" +
      "SELECT DISTINCT s.sigle AS value                                                        " +
      "FROM   timref.local@timref_oper l,                                                      " +
      "       ouvrage o,                                                                       " +
      "       sigle s                                                                          " +
      "WHERE  o.numero = l.numbat                                                              " +
      "AND    o.numero = s.numero                                                              " +
      "AND    o.actif  = 'Y'                                                                   " +
      "AND    o.numero = :buildingNumber                                                       " +
      "AND    s.sigle  LIKE UPPER(:query || '%')                                               " +
      "ORDER BY 1", nativeQuery = true)
  List<BuildingName> findByBuildingNumber(@Param("query") String query, @Param("buildingNumber") String buildingNumber);
}
