/**
 *
 */
package cern.modesti.request;

import java.io.Serializable;

import cern.modesti.repository.mongo.domain.Domain;
import cern.modesti.repository.mongo.domain.DomainRepository;
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

  @Autowired
  private DomainRepository domainRepository;

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
    } else if (entityType.equals(Domain.class)) {
      logger.trace("fromRequestId() converting domain id: " + id);

      Domain domain = domainRepository.findOneByNameIgnoreCase(id);

      if (domain != null) {
        return domain.getId();
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

    } else if (entityType.equals(Domain.class)) {
      logger.trace("toRequestId() converting domain id : " + id);
      Domain domain = domainRepository.findOne(id.toString());

      if (domain != null) {
        return domain.getName();
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
