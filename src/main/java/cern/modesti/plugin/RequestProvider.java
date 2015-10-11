package cern.modesti.plugin;

import cern.modesti.plugin.metadata.MetadataProvider;
import cern.modesti.request.Request;
import org.springframework.plugin.core.Plugin;

/**
 * TODO
 *
 * TODO get uploaded request parser via plugin method?
 * TODO what about configuration of additional DB connections inside plugins?
 *
 * @author Justin Lewis Salmon
 */
public abstract class RequestProvider implements Plugin<Request>, MetadataProvider {

  /**
   * Returns if a plugin should be invoked according to the given request.
   *
   * @param request
   * @return true if the plugin should be invoked for this request, false otherwise
   */
  @Override
  public boolean supports(Request request) {
    return getMetadata().getName().equals(request.getDomain());
  }
}
