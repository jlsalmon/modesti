package cern.modesti.plugin.spi;

import cern.modesti.request.Request;

/**
 * By implementing this interface the plugins can handle their own authorization
 *
 * @author Martin Flamm
 */
public interface AuthorizationProvider extends ExtensionPoint {

  boolean canDelete(Request request);
}
