
package cern.modesti.request;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Repository for creating, reading, updating and deleting {@link Request}
 * instances.
 * <p>
 * By setting the excerpt projection on the interface, retrieving a list of
 * requests will in fact return a list of {@link RequestProjection} instances.
 * This reduces backend load greatly. Retrieving a single request will still
 * return the full request, unless the {@link RequestProjection}is explicitly
 * requested.
 * <p>
 * <b>Note:</b> plugins should not use this repository directly, but rather
 * use {@link RequestService} instead.
 *
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(excerptProjection = RequestProjection.class)
public interface RequestRepository extends MongoRepository<Request, String>, QueryDslPredicateExecutor<Request> {

  /**
   * Retrieve a single {@link Request} instance.
   *
   * @param requestId the id of the request to retrieve
   * @return the request instance, or null if no request was found with the
   * given id
   */
  Request findOneByRequestId(@Param("requestId") String requestId);

  /**
   * Save a single {@link Request} instance.
   * <p>
   * If a request already exists with the same id as the given request, it will
   * be overwritten. Otherwise, it wil be created.
   * <p>
   * A user may only save a request if:
   * <ul>
   * <li>They are the original creator</li>
   * <li>They are the current assignee</li>
   * <li>They are an administrator</li>
   * </ul>
   *
   * @param request the request to save
   * @return the newly saved request
   */
  @PreAuthorize("@authService.isCreator(#request, principal) or @authService.isAuthorised(#request, principal) or hasRole('modesti-administrators')")
  @Override
  Request save(@Param("request") Request request);

  @PreAuthorize("@authService.canDelete(#request, principal)")
  @Override
  void delete(String id);

  @PreAuthorize("@authService.canDelete(#request, principal)")
  @Override
  void delete(@Param("request") Request request);

  @PreAuthorize("hasRole('modesti-administrators')")
  @Override
  void deleteAll();
}
