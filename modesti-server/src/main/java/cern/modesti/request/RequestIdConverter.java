package cern.modesti.request;

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
public class RequestIdConverter implements BackendIdConverter {

  @Autowired
  private RequestRepository requestRepository;

  @Override
  public Serializable fromRequestId(String id, Class<?> entityType) {

    if (entityType.equals(RequestImpl.class)) {
      Request request = requestRepository.findOneByRequestId(id);

      if (request != null) {
        return request.getId();
      }
    }

    return id;
  }

  @Override
  public String toRequestId(Serializable id, Class<?> entityType) {

    if (entityType.equals(RequestImpl.class)) {
      Request request = requestRepository.findOne(id.toString());

      if (request != null) {
        return request.getRequestId();
      }
    }

    return id.toString();
  }

  @Override
  public boolean supports(Class<?> delimiter) {
    return Request.class.equals(delimiter) || RequestImpl.class.equals(delimiter);
  }
}
