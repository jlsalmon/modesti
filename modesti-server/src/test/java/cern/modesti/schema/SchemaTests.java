package cern.modesti.schema;

import cern.modesti.BaseIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * @author Justin Lewis Salmon
 */
public class SchemaTests extends BaseIntegrationTest {

  @Autowired
  private SchemaRepository schemaRepository;

  @Test
  public void schemaIsLoaded() {
    assertTrue(schemaRepository.existsById("DUMMY"));
  }
}
