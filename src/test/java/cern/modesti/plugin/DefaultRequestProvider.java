package cern.modesti.plugin;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.metadata.DefaultPluginMetadata;
import cern.modesti.plugin.metadata.PluginMetadata;
import cern.modesti.request.Request;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Component
@Profile({"test"})
public class DefaultRequestProvider extends RequestProvider {

  public static final String DEFAULT = "DEFAULT";

  @Override
  public boolean validate(Request request) {
    return true;
  }

  @Override
  public boolean configure(Request request) {
    return true;
  }

  @Override
  public PluginMetadata getMetadata() {
    return new DefaultPluginMetadata(DEFAULT);
  }
}
