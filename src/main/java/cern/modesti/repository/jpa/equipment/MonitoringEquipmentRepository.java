package cern.modesti.repository.jpa.equipment;

import cern.modesti.repository.base.ReadOnlyRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface MonitoringEquipmentRepository extends ReadOnlyRepository<MonitoringEquipment, Integer> {

  @RestResource(path = "find", rel = "find")
  List<MonitoringEquipment> findByValueStartsWithIgnoreCase(@Param("query") String query);

  @RestResource(exported = false)
  MonitoringEquipment findOneByValue(@Param("query") String query);

  @RestResource(exported = false)
  MonitoringEquipment findOneByName(@Param("query") String query);
}
