package cern.modesti.workflow.notification;

import cern.modesti.request.Request;
import cern.modesti.schema.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Map;

/**
 * This class represents a single email notification that can be sent via the
 * {@link NotificationService}.
 * <p>
 * The body of the email is loaded from a Thymeleaf template pointed to by the
 * {@link #template} field. Arbitrary parameters can be passed to this template
 * via {@link #templateParameters}.
 * <p>
 * A notification may be sent to one or more recipients.
 *
 * @author Justin Lewis Salmon
 */
@Data
@Builder
public class Notification {

  private final String subject;

  @Singular
  private final List<String> recipients;

  private final String template;

  @Singular
  private Map<String, Object> templateParameters;

  private Request request;

  private Schema schema;
}
