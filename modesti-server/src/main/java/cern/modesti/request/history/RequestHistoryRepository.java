package cern.modesti.request.history;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author Justin Lewis Salmon
 */
@RepositoryRestResource(path = "requestHistories", collectionResourceRel = "requestHistories", itemResourceRel = "requestHistory")
public interface RequestHistoryRepository extends MongoRepository<RequestHistoryImpl, String> {

  RequestHistoryImpl findOneByRequestId(String requestId);
}
