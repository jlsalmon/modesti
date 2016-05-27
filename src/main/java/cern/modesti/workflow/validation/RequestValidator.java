package cern.modesti.workflow.validation;

import cern.modesti.request.Request;
import cern.modesti.schema.Schema;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestValidator {

  boolean validateRequest(Request request, Schema schema);
}
