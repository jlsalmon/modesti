package cern.modesti.plugin.metadata;

import cern.modesti.request.RequestType;
import lombok.Data;
import org.springframework.util.Assert;

import static java.lang.String.format;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class DefaultPluginMetadata implements PluginMetadata {

  public static final String DEFAULT_CREATE_PROCESS = "create";
  public static final String DEFAULT_UPDATE_PROCESS = "update";
  public static final String DEFAULT_DELETE_PROCESS = "create";

  private String name;

  /**
   * Creates a new instance of {@code DefaultPluginMetadata}.
   *
   * @param name must not be {@literal null}.
   */
  public DefaultPluginMetadata(String name) {
    Assert.hasText(name, "Name must not be null or empty!");
    this.name = name;
  }

  @Override
  public String getProcessKey(RequestType type) {
    switch (type) {
      case CREATE:
        return DEFAULT_CREATE_PROCESS;
      case MODIFY:
        return DEFAULT_UPDATE_PROCESS;
      case DELETE:
        return DEFAULT_DELETE_PROCESS;
      default:
        throw new IllegalArgumentException(format("Request type %s is not supported", type));
    }
  }
}
