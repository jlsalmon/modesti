package cern.modesti.request;

import cern.modesti.repository.jpa.subsystem.SubSystem;
import cern.modesti.security.ldap.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;
import java.util.Set;

/**
 * Excerpt projection for a {@link Request}. Only the fields returned by the mothods specified in this interface will be returned when this projection is used.
 *
 * @author Justin Lewis Salmon
 */
@Projection(name = "skinny", types = {Request.class})
public interface SkinnyRequest {

  String getRequestId();

  RequestStatus getStatus();

  void setStatus(RequestStatus status);

  RequestType getType();

  User getCreator();

  String getDescription();

  String getDomain();

  SubSystem getSubsystem();

  Set<String> getCategories();
}
