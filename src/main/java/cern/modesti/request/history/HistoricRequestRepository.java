package cern.modesti.request.history;

import cern.modesti.request.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Justin Lewis Salmon
 */
public interface HistoricRequestRepository extends MongoRepository<Request, String> {
}
