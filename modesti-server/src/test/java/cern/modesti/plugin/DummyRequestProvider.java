package cern.modesti.plugin;

import cern.modesti.plugin.metadata.PluginMetadata;
import cern.modesti.plugin.metadata.SimplePluginMetadata;
import org.springframework.stereotype.Component;

/**
 * @author Justin Lewis Salmon
 */
@Component
public class DummyRequestProvider extends RequestProvider {

  public static final String DUMMY = "DUMMY";

  @Override
  public PluginMetadata getMetadata() {
    return new SimplePluginMetadata(DUMMY, "modesti-dummy-users", "dummy");
  }
}
