package cern.modesti.workflow.validation;

import cern.modesti.plugin.spi.ExtensionPoint;
import cern.modesti.request.Request;
import cern.modesti.schema.Schema;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestValidator extends ExtensionPoint {

  boolean validateRequest(Request request, Schema schema);
}
