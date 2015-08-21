package cern.modesti.plugin.metadata;

/**
 * Interface for plugins providing metadata information.
 *
 * @author Justin Lewis Salmon
 */
public interface MetadataProvider {

  /**
   * Returns the plugins metadata.
   *
   * @return the plugins metadata
   */
  PluginMetadata getMetadata();
}
