package cern.modesti.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Excerpt projection for a {@link Request}. Only the fields returned by the
 * methods specified in this interface will be returned when this projection
 * is used.
 *
 * @author Justin Lewis Salmon
 */
@Projection(types = {RequestImpl.class})
public interface RequestProjection {

  String getRequestId();

  String getStatus();

  RequestType getType();

  String getCreator();

  String getAssignee();

  String getDescription();

  String getDomain();

  @Value("#{target.createdAt.millis}")
  Long getCreatedAt();

//  @Value("#{target.lastModified.millis}")
//  Long getLastModified();
}
