
package cern.modesti.request;

import cern.modesti.security.ldap.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import cern.modesti.request.Request;
import cern.modesti.request.SkinnyRequest;

/**
 * By setting the excerpt projection on the interface, retrieving a list of requests will in fact return a list of skinny requests. This reduces backend load
 * greatly. Retrieving a single request will still return the full request, unless the skinny projection is explicitly requested.
 *
 * @author Justin Lewis Salmon
 */

@RepositoryRestResource(excerptProjection = SkinnyRequest.class)
public interface RequestRepository extends MongoRepository<Request, String> {

  /**
   * @param requestId
   *
   * @return
   */
  Request findOneByRequestId(@Param("requestId") String requestId);

  /**
   * @param criteria
   * @param page
   *
   * @return
   */
  Page<Request> findAllByOrderByScoreDesc(@Param("q") TextCriteria criteria, Pageable page);

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
  @Query("{ '$and': [" +
      "{ 'status': { '$regex': '?0' } }, " +
      "{ 'domain': { '$regex': '?1' } }, " +
      "{ 'type':   { '$regex': '?2' } }, " +
      "{ 'subsystem'   : { '$regex': '?3' } }, " +
//      "{ 'subsystem.system'   : { '$regex': '?3' } }, " +
//      "{ 'subsystem.subsystem': { '$regex': '?4' } }, " +
      "{ 'creator.username'   : { '$regex': '?4' } } ] }")
  Page<Request> find(@Param("status") String status, @Param("domain") String domain, @Param("type") String type, @Param("subsystem") String subsystem, @Param
      ("creator") String creator, Pageable page);

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

  @PreAuthorize("hasRole('modesti-administrators')")
  @Override
  void delete(String id);

  @PreAuthorize("hasRole('modesti-administrators')")
  @Override
  void delete(Request request);

  @PreAuthorize("hasRole('modesti-administrators')")
  @Override
  void deleteAll();
}
