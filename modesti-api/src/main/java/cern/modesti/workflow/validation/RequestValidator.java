package cern.modesti.workflow.validation;

import cern.modesti.plugin.spi.ExtensionPoint;
import cern.modesti.point.Point;
import cern.modesti.request.Request;
import cern.modesti.schema.Schema;

/**
 * Interface to be implemented by the request validators for each plugin
 * @author Justin Lewis Salmon
 * @author Ivan Prieto Barreiro
 */
public interface RequestValidator extends ExtensionPoint {

  /**
   * Request pre-validation checks that the minimum required data is provided in the request.
   * The method is triggered from requests coming from REST API without using the MODESTI front-end.
   * @param request The request to validate
   * @param schema The plugin schema
   * @return TRUE if and only if the request contains the minimum required data 
   */
  boolean preValidateRequest(Request request, Schema schema);
  
  /**
   * Validates the request according to the schema definition.
   * Error messages will be attached to each individual point and be retrievable via
   * {@link Point#getErrors()}.
   * @param request The request to validate
   * @param schema The plugin schema
   * @return TRUE if and only if the request is valid. 
   */
  boolean validateRequest(Request request, Schema schema);
}
