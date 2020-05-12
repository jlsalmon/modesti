
package cern.modesti.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import com.querydsl.core.types.Predicate;

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
@RepositoryRestResource(path = "requests", collectionResourceRel = "requests", itemResourceRel = "request", exported=false)
public interface RequestRepository extends MongoRepository<RequestImpl, String>, QuerydslPredicateExecutor<RequestImpl> {
  
  /**
   * Retrieve a single {@link Request} instance.
   *
   * @param requestId the id of the request to retrieve
   * @return the request instance, or null if no request was found with the
   * given id
   */
  RequestImpl findOneByRequestId(@Param("requestId") String requestId);

  /**
   * Get a page of requests using the {@link RequestProjection} projection
   * @param pageable Pagination information
   * @return Page of projected requests 
   */
  Page<RequestProjection> findAllProjectedBy(Pageable pageable);
  
  /**
   * Get a page of requests fulfilling the provided predicate
   * @param predicate Predicate for searching requests
   * @param pageable Pagination information
   * @return Page of projected requests 
   */
  Page<RequestImpl> findAll(Predicate predicate, Pageable pageable);
  
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
  @PreAuthorize("@authService.canSave(#request, principal)")
  @Override
  RequestImpl save(@Param("request") RequestImpl request);

  @PreAuthorize("@authService.canDelete(#request, principal)")
  @Override
  void deleteById(String id);

  @PreAuthorize("@authService.canDelete(#request, principal)")
  @Override
  void delete(@Param("request") RequestImpl request);

  @PreAuthorize("hasRole('modesti-administrators')")
  @Override
  void deleteAll();
}
