package cern.modesti.plugin.metadata;

import cern.modesti.request.RequestType;

/**
 * Basic interface to define a set of metadata information for plugins.
 *
 * @author Justin Lewis Salmon
 */
public interface PluginMetadata {

  /**
   * Returns a unique plugin name. Plugins returning a metadata implementation have to ensure uniqueness of this name.
   *
   * @return the name of the plugin
   */
  String getName();

  /**
   * Returns the identifier of the workflow process to be used based on the given request type.
   *
   * @param type
   * @return
   */
  String getProcessKey(RequestType type);
}
