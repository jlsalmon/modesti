package cern.modesti.repository.mongo.domain;

import cern.modesti.model.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Component
public class DomainIdConverter implements BackendIdConverter {
  Logger logger = LoggerFactory.getLogger(DomainIdConverter.class);

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

    if (entityType.equals(Domain.class)) {
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

    if (entityType.equals(Domain.class)) {
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
