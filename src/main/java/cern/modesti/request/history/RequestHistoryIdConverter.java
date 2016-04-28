package cern.modesti.request.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.stereotype.Component;

import java.io.Serializable;

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

  @Override
  public Serializable fromRequestId(String id, Class<?> entityType) {
    RequestHistory entry = requestHistoryRepository.findOneByRequestId(id);
    if (entry == null) {
      return id;
    }
    return entry.getId();
  }

  @Override
  public String toRequestId(Serializable id, Class<?> entityType) {
    RequestHistory entry = requestHistoryRepository.findOne(id.toString());
    if (entry == null) {
      return id.toString();
    }
    return entry.getRequestId();
  }

  @Override
  public boolean supports(Class<?> delimiter) {
    return RequestHistory.class.equals(delimiter);
  }
}
