/**
 *
 */
package cern.modesti.upload;

import cern.modesti.request.counter.CounterServiceImpl;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.upload.parser.RequestParser;
import cern.modesti.request.RequestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Justin Lewis Salmon
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UploadServiceTest {

  private static final Logger LOG = LoggerFactory.getLogger(UploadServiceTest.class);

  /**
   * The class to be tested
   */
  @InjectMocks
  UploadService uploadService;

  @Mock
  RequestParser parser;

  @Mock
  RequestRepository requestRepository;

  @Mock
  CounterServiceImpl counterService;

  @Mock
  SchemaRepository schemaRepository;

  @Test
  public void empty() {

  }
}
