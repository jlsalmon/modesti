package cern.modesti.request.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

/**
 * This class is used by the Spring Data infrastructure to convert to/from the
 * internal MongoDB ObjectId and a human-readable integer id that we generate.
 *
 * @author Justin Lewis Salmon
 */
@Component
public class RequestHistoryIdConverter implements BackendIdConverter {

  @Autowired
  private RequestHistoryRepository requestHistoryRepository;
  
  @Autowired
  private RequestHistoryServiceImpl requestHistoryService;

  @Override
  public Serializable fromRequestId(String id, Class<?> entityType) {

    if (entityType.equals(RequestHistoryImpl.class)) {
      RequestHistory entry = requestHistoryService.findOneByRequestId(id);
      if (entry != null) {
        return entry.getId();
      }
    }

    return id;
  }

  @Override
  public String toRequestId(Serializable id, Class<?> entityType) {

    if (entityType.equals(RequestHistoryImpl.class)) {
      Optional<RequestHistoryImpl> entry = requestHistoryRepository.findById(id.toString());

      if (entry.isPresent()) {
        return entry.get().getRequestId();
      }
    }

    return id.toString();
  }

  @Override
  public boolean supports(Class<?> delimiter) {
    return RequestHistory.class.equals(delimiter) || RequestHistoryImpl.class.equals(delimiter);
  }
}
