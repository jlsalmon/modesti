package cern.modesti.workflow.notification;

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
@AllArgsConstructor
@Builder
public class Notification {
  private String subject;
  private String template;
  @Singular
  private List<String> recipients;
  @Singular
  private Map<String, Object> parameters;
}
