package cern.modesti.schema.processor;

import cern.modesti.schema.Schema;

/**
 * @author Justin Lewis Salmon
 */
public interface SchemaPostProcessor {

  Schema postProcess(Schema schema);
}
