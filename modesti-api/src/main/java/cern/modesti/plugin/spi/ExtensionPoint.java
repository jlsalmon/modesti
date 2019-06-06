package cern.modesti.plugin.spi;

/**
 * @author Martin Flamm
 */
public interface ExtensionPoint {

  /**
   * Gets the plugin identifier
   * @return plugin identifier
   */
  String getPluginId();
}