package cern.modesti.workflow.configuration;

/**
 * @author Justin Lewis Salmon
 */
public class ConfigurationException extends RuntimeException {

  private static final long serialVersionUID = -2584910082994169155L;

  /**
   *
   */
  public ConfigurationException(String message) {
    super(message);
  }

  /**
   *
   */
  public ConfigurationException(Exception e) {
    super(e);
  }
}