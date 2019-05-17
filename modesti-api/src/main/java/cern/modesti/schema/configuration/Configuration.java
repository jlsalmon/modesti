package cern.modesti.schema.configuration;

import java.io.Serializable;

/**
 * Schema configuration options
 *  
 * @author Ivan Prieto Barreiro
 */
public interface Configuration extends Serializable {

  /**
   * Specifies if the creation of requests from the UI is allowed
   * @return TRUE if and only if the creation of requests from the UI is allowed
   */ 
  boolean isCreateFromUi();
  
  /**
   * Specifies if the cloning of requests from the UI is allowed
   * @return TRUE if and only if the cloning of requests from the UI is allowed
   */ 
  boolean isCloneFromUi();
  
  /**
   * Specifies if the clone requests must show the schema fields
   * @return TRUE if and only if the clone requests must show the schema fields
   */
  boolean isShowFieldsOnClone();
  
  /**
   * Specifies if the delete requests must show the schema fields
   * @return TRUE if and only if the delete requests must show the schema fields
   */
  boolean isShowFieldsOnDelete();
}
