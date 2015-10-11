package cern.modesti.repository.location.functionality;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import cern.modesti.repository.base.ReadOnlyRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface FunctionalityRepository extends ReadOnlyRepository<Functionality, String> {

  /**
   * TODO
   *
   * We don't need a manual query here, as the functionalities table is very
   * simple. So we annotate the {@link Functionality} class with the necessary table and
   * column names and let Spring create a query automatically based on the
   * method name.
   *
   * We also rename the REST endpoint for this resource via the
   * {@link RestResource} annotation to make things nicer.
   *
   * @param query
   * @return
   */
  @RestResource(rel = "find", path = "find")
  @Cacheable("functionalities")
  List<Functionality> findByValueStartsWithIgnoreCase(@Param("query") String query);
}
