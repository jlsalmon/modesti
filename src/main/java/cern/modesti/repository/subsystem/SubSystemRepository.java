package cern.modesti.repository.subsystem;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(collectionResourceRel = "subsystems", path = "subsystems")
public interface SubSystemRepository extends ReadOnlyRepository<SubSystem, String> {

  /**
   * @param query
   *
   * @return
   */
  @Query(value = "" +
      "SELECT tes_system_name || ' ' || tess_subsystem_name as value,                        " +
      "       tes_system_name as system,                                                     " +
      "       tes_system_code as systemCode,                                                 " +
      "       tess_subsystem_id as id,                                                       " +
      "       tess_subsystem_name as subsystem,                                              " +
      "       tess_subsystem_code as subsystemCode                                           " +
      "FROM   vpts_sysdet " +
      "WHERE  tes_system_name || ' ' || tess_subsystem_name LIKE UPPER('%' || :query || '%') " +
      "ORDER BY 1",
      nativeQuery = true)
  @Cacheable("subsystems")
  List<SubSystem> find(@Param("query") String query);

  //  @Override
  //  @Query(value = "SELECT rv_low_value as name FROM cg_ref_codes WHERE rv_domain = 'PTDATATYPES'", nativeQuery = true)
  //  public List<SubSystem> findAll();
}
