package cern.modesti.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection for a {@link Request}. Only the fields returned by the
 * methods specified in this interface will be returned when this projection
 * is used.
 *
 * @author Justin Lewis Salmon
 */
@Projection(types = {RequestImpl.class})
public interface RequestProjection {

  /**
   * Gets the request identifier
   * @return Request identifier
   */
  String getRequestId();

  /**
   * Gets the request status (IN_PROGRESS, FOR_CONFIGURATION, etc)
   * @return Request status
   */
  String getStatus();

  /**
   * Gets the request type (CREATE, UPDATE, DELETE).
   * @return The request type
   */
  RequestType getType();

  /**
   * Gets the request creator
   * @return Request creator
   */
  String getCreator();
  
  /**
   * Gets the request assignee 
   * @return Request assignee
   */
  String getAssignee();

  /**
   * Gets the request description
   * @return Request description
   */
  String getDescription();

  /**
   * Gets the request domain (TIM, CSAM, PSEN, ...)
   * @return Request domain
   */
  String getDomain();

  /**
   * Gets the request creation date
   * @return Request creation date
   */
  @Value("#{target.createdAt.millis}")
  Long getCreatedAt();

}
