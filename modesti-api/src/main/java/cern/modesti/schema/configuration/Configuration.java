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
   * @return
   */ 
  boolean isCreateFromUi();
  
  /**
   * Specifies if the cloning of requests from the UI is allowed
   * @return
   */ 
  boolean isCloneFromUi();
  
  /**
   * Specifies if the clone requests must show the schema fields
   * @return
   */
  boolean isShowFieldsOnClone();
  
  /**
   * Specifies if the delete requests must show the schema fields
   * @return
   */
  boolean isShowFieldsOnDelete();
}
