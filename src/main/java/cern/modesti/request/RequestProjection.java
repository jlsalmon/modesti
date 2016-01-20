package cern.modesti.request;

import cern.modesti.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.Set;

/**
 * Excerpt projection for a {@link Request}. Only the fields returned by the methods specified in this interface will be returned when this projection is used.
 *
 * @author Justin Lewis Salmon
 */
@Projection(types = {Request.class})
public interface RequestProjection {

  String getRequestId();

  String getStatus();

  RequestType getType();

  User getCreator();

  User getAssignee();

  String getDescription();

  String getDomain();

  String getSubsystem();

  @Value("#{target.createdAt.millis}")
  Long getCreatedAt();

  @Value("#{target.lastModified.millis}")
  Long getLastModified();
}
