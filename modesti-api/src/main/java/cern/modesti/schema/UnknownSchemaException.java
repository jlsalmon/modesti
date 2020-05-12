package cern.modesti.schema;

/**
 * Exception thrown when the schema is unknown or not available in the repository
 *  
 * @author Ivan Prieto Barreiro
 */
public class UnknownSchemaException extends RuntimeException {
  private static final long serialVersionUID = 128272868113460424L;

  /**
   * Class constructor
   * @param id The schema identifier
   */
  public UnknownSchemaException(String id) {
    super("The schema was not found in the repository: " + id);
  }
}
