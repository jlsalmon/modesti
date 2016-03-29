package cern.modesti.request.history;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestHistoryRepository extends MongoRepository<RequestHistory, String> {

  RequestHistory findOneByRequestId(String requestId);
}
