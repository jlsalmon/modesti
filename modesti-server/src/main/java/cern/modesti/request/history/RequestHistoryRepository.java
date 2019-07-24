package cern.modesti.request.history;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(path = "requestHistories", collectionResourceRel = "requestHistories", itemResourceRel = "requestHistory")
public interface RequestHistoryRepository extends MongoRepository<RequestHistoryImpl, String> {

  /**
   * Find a single request by its request identifier
   * @param requestId The request identifier
   * @return The request with the specified identifier or null if it was not found
   */
//  RequestHistoryImpl findOneByRequestId(String requestId);
  
  /**
   * Finds all the requests with the specified request identifier. This method was added
   * after a bug was found, where the request history contains two documents for a requestId
   * @param requestId The request identifier
   * @return Request list with the specified identifier or null/empty list if it was not found
   */
  List<RequestHistoryImpl> findAllByRequestId(String requestId);
}
