package cern.modesti.schema.configuration;

import java.io.Serializable;

/**
 * Schema configuration options
 *  
 * @author Ivan Prieto Barreiro
 */
public interface Configuration extends Serializable {

  /**
   * Specifies if the creation of requests from the UI is disabled
   * @return
   */ 
  boolean isDisableCreateFromUi();
}
