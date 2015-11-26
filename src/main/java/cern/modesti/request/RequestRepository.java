
package cern.modesti.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
   *
   * @return
   */
  Request findOneByRequestId(@Param("requestId") String requestId);

  /**
   * Find requests by either their status, domain, type, system, subsystem, or creator.
   *
   * TODO: this way of querying is probably horribly inefficient. It could probably benefit from indexing of some kind.
   *
   * TODO replace this with QueryDsl
   *
   * @param status
   * @param domain
   * @param type
   * @param subsystem
   * @param creator
   * @param page
   *
   * @return
   */
  @Query("{ '$and': [                                      " +
         "    { 'status':            { '$regex': '?0' } }, " +
         "    { 'domain':            { '$regex': '?1' } }, " +
         "    { 'type':              { '$regex': '?2' } }, " +
         "    { 'subsystem':         { '$regex': '?3' } }, " +
         "    { 'creator.username':  { '$regex': '?4' } }, " +
         "    { 'assignee.username': { '$regex': '?5' } }  " +
         "  ]                                              " +
         "}                                                ")
  Page<Request> find(@Param("status") String status, @Param("domain") String domain, @Param("type") String type, @Param("subsystem") String subsystem, @Param
      ("creator") String creator, @Param("assignee") String assignee, Pageable page);

  /**
   * TODO
   *
   * A user may save a request if:
   *
   * - They are the original creator
   * - They are an approver or cabler and are assigned to the approval/addressing/cabling task
   * - They are an administrator
   *
   * TODO cover this with test cases
   *
   * @param request
   *
   * @return
   */
  @PreAuthorize("@authService.isCreator(#request, principal) or @authService.isAssigned(#request, principal) or hasRole('modesti-administrators')")
  @Override
  Request save(@Param("request") Request request);

  @PreAuthorize("@authService.isCreator(#request, principal) or @authService.isAssigned(#request, principal) or hasRole('modesti-administrators')")
  @Override
  void delete(String id);

  @PreAuthorize("@authService.isCreator(#request, principal) or @authService.isAssigned(#request, principal) or hasRole('modesti-administrators')")
  @Override
  void delete(@Param("request") Request request);

  @PreAuthorize("hasRole('modesti-administrators')")
  @Override
  void deleteAll();
}
