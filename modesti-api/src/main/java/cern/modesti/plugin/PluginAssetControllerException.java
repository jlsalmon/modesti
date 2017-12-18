package cern.modesti.plugin;

/**
 * Exception to be thrown by the PluginAssetController when the plugin assets 
 * could not be loaded.
 *  
 * @author Ivan Prieto Barreiro
 */
public class PluginAssetControllerException extends Exception {

  /**
   * Constructor for the exception
   * @param message Error message
   */
  public PluginAssetControllerException(String message) {
    super(message);
  }

}
