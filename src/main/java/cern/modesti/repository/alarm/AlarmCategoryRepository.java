package cern.modesti.repository.alarm;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface AlarmCategoryRepository extends ReadOnlyRepository<AlarmCategory, String>{

  @RestResource(rel = "find", path = "find")
  List<AlarmCategory> findByValueContainingIgnoreCase(@Param("query") String name);
}
