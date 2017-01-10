package cern.modesti.plugin;

import cern.modesti.plugin.metadata.MetadataProvider;
import cern.modesti.request.Request;
import org.springframework.plugin.core.Plugin;

/**
 * An implementation must extend this class to be registered as a plugin.
 *
 * @author Justin Lewis Salmon
 */
public abstract class RequestProvider implements Plugin<Request>, MetadataProvider {

  /**
   * Decides if a plugin should be invoked according to the given request.
   *
   * @param request the request instance
   * @return true if the plugin should be invoked for this request, false otherwise
   */
  @Override
  public boolean supports(Request request) {
    return getMetadata().getId().equals(request.getDomain());
  }
}
