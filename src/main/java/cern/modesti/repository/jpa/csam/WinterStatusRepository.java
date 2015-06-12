/**
 *
 */
package cern.modesti.repository.jpa.csam;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import cern.modesti.model.csam.WinterStatus;
import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface WinterStatusRepository extends ReadOnlyRepository<WinterStatus, String>{

  @Override
  @Query(value = "SELECT TO_NUMBER(rv_low_value) as value "
               + "FROM   timref.cg_ref_codes@timref_oper "
               + "WHERE  rv_domain = 'WINTER_STATUS' ORDER BY 1", nativeQuery = true)
  List<WinterStatus> findAll();
}
