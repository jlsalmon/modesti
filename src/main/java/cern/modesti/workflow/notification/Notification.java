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
 * TODO
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
