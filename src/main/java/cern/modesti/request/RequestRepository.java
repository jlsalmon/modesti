
package cern.modesti.request;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * By setting the excerpt projection on the interface, retrieving a list of requests will in fact return a list of skinny requests. This reduces backend load
 * greatly. Retrieving a single request will still return the full request, unless the skinny projection is explicitly requested.
 *
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(excerptProjection = RequestProjection.class)
public interface RequestRepository extends MongoRepository<Request, String>, QueryDslPredicateExecutor<Request> {

  /**
   * @param requestId
   * @return
   */
  Request findOneByRequestId(@Param("requestId") String requestId);

  /**
   * A user may save a request if:
   * <p>
   * - They are the original creator
   * - They are the current assignee
   * - They are an administrator
   * <p>
   * TODO cover this with test cases
   *
   * @param request
   * @return
   */
  @PreAuthorize("@authService.isCreator(#request, principal) or @authService.isAuthorised(#request, principal) or hasRole('modesti-administrators')")
  @Override
  Request save(@Param("request") Request request);

  @PreAuthorize("@authService.isCreator(#request, principal) or hasRole('modesti-administrators')")
  @Override
  void delete(String id);

  @PreAuthorize("@authService.isCreator(#request, principal) or hasRole('modesti-administrators')")
  @Override
  void delete(@Param("request") Request request);

  @PreAuthorize("hasRole('modesti-administrators')")
  @Override
  void deleteAll();
}
