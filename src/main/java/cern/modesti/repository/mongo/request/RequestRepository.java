
package cern.modesti.repository.mongo.request;

import cern.modesti.request.Request;
import cern.modesti.request.SkinnyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * By setting the excerpt projection on the interface, retrieving a list of requests will in fact return a list of skinny requests. This reduces load greatly.
 * Retrieving a single request will still return the full request, unless the skinny projection is explicitly requested.
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
   * TODO: this way of querying is probably horribly inefficient. It could probably benefit from indexing of some kind.
   *
   * @param status
   * @param domain
   * @param type
   * @param system
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
      "{ 'subsystem.system'   : { '$regex': '?3' } }, " +
      "{ 'subsystem.subsystem': { '$regex': '?4' } }, " +
      "{ 'creator.username'   : { '$regex': '?5' } } ] }")
  Page<Request> find(@Param("status") String status, @Param("domain") String domain, @Param("type") String type, @Param("system") String system, @Param
      ("subsystem") String subsystem, @Param("creator") String creator, Pageable page);
}
