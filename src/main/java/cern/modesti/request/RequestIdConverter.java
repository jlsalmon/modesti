/**
 *
 */
package cern.modesti.request;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.stereotype.Component;

import cern.modesti.repository.mongo.request.RequestRepository;

/**
 * @author Justin Lewis Salmon
 *
 */
@Component
public class RequestIdConverter implements BackendIdConverter {

  Logger logger = LoggerFactory.getLogger(RequestIdConverter.class);

  @Autowired
  private RequestRepository requestRepository;

  /**
   * TODO
   *
   * @param id
   * @param entityType
   * @return
   */
  @Override
  public Serializable fromRequestId(String id, Class<?> entityType) {

    if (entityType.equals(Request.class)) {
      logger.trace("fromRequestId() converting request id: " + id);

      Request request = requestRepository.findOneByRequestId(id);

      if (request != null) {
        return request.getId();
      }
    }

    return id;
  }

  /**
   * TODO
   *
   * @param id
   * @param entityType
   * @return
   */
  @Override
  public String toRequestId(Serializable id, Class<?> entityType) {

    if (entityType.equals(Request.class)) {
      logger.trace("toRequestId() converting request id : " + id);
      Request request = requestRepository.findOne(id.toString());

      if (request != null) {
        return request.getRequestId().toString();
      }

    }

    return id.toString();
  }

  /**
   * TODO
   *
   * @param delimiter
   * @return
   */
  @Override
  public boolean supports(Class<?> delimiter) {
    return true;
  }
}
