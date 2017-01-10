package cern.modesti.schema.spi;

import cern.modesti.schema.Schema;

/**
 * SPI for performing post processing tasks on schemas once they are loaded
 * from the classpath.
 *
 * @author Justin Lewis Salmon
 */
public interface SchemaPostProcessor {

  /**
   * Process a single loaded schema.
   *
   * @param schema the newly loaded schema
   * @return the modified schema after it has been processed
   */
  Schema postProcess(Schema schema);
}
