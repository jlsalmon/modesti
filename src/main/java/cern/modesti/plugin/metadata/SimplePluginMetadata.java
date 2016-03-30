package cern.modesti.plugin.metadata;

import cern.modesti.request.RequestType;
import lombok.Data;
import org.springframework.util.Assert;

/**
 * A simple implementation of {@link PluginMetadata} that uses the same workflow
 * process for CREATE, UPDATE and DELETE requests.
 *
 * @author Justin Lewis Salmon
 */
@Data
public class SimplePluginMetadata implements PluginMetadata {

  private String name;
  private String processKey;

  /**
   * Creates a new instance of {@code SimplePluginMetadata}.
   *
   * @param name must not be {@literal null}.
   */
  public SimplePluginMetadata(String name, String processKey) {
    Assert.hasText(name, "Name must not be null or empty!");

    this.name = name;
    this.processKey = processKey;
  }

  @Override
  public String getProcessKey(RequestType type) {
    return processKey;
  }
}
