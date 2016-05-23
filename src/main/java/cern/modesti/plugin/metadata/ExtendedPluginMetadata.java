package cern.modesti.plugin.metadata;

import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import lombok.Data;
import org.springframework.util.Assert;

import static java.lang.String.format;

/**
 * A {@link PluginMetadata} implementation for plugins that wish to have
 * separate workflow processes for each {@link RequestType}.
 *
 * @author Justin Lewis Salmon
 */
@Data
public class ExtendedPluginMetadata implements PluginMetadata {

  private String name;
  private String authorisationGroup;
  private String createProcessKey;
  private String updateProcessKey;
  private String deleteProcessKey;

  /**
   * Creates a new instance of {@code DefaultPluginMetadata}.
   *
   * @param name must not be {@literal null}.
   * @param authorisationGroup must not be {@literal null}.
   * @param createProcessKey must not be {@literal null}.
   * @param updateProcessKey must not be {@literal null}.
   * @param deleteProcessKey must not be {@literal null}.
   */
  public ExtendedPluginMetadata(String name, String authorisationGroup, String createProcessKey, String updateProcessKey, String deleteProcessKey) {
    Assert.hasText(name, "Name must not be null or empty!");
    Assert.hasText(authorisationGroup, "Authorisation group must not be null or empty!");
    Assert.hasText(createProcessKey, "Create process key must not be null or empty!");
    Assert.hasText(updateProcessKey, "Update process key must not be null or empty!");
    Assert.hasText(deleteProcessKey, "Delete process key must not be null or empty!");

    this.name = name;
    this.authorisationGroup = authorisationGroup;
    this.createProcessKey = createProcessKey;
    this.updateProcessKey = updateProcessKey;
    this.deleteProcessKey = deleteProcessKey;
  }

  @Override
  public String getAuthorisationGroup(Request ignored) {
    return authorisationGroup;
  }

  @Override
  public String getProcessKey(RequestType type) {
    switch (type) {
      case CREATE:
        return createProcessKey;
      case UPDATE:
        return updateProcessKey;
      case DELETE:
        return deleteProcessKey;
      default:
        throw new IllegalArgumentException(format("Request type %s is not supported", type));
    }
  }
}
