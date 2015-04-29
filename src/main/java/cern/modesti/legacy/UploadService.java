/**
 *
 */
package cern.modesti.legacy;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.request.Request;
import cern.modesti.request.Request.RequestStatus;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.schema.Schema;
import cern.modesti.repository.mongo.schema.SchemaRepository;
import cern.modesti.legacy.parser.RequestParser;
import cern.modesti.legacy.parser.RequestParserFactory;

/**
 * @author Justin Lewis Salmon
 *
 */
@Service
public class UploadService {

  private static final Logger LOG = LoggerFactory.getLogger(UploadService.class);

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private CounterService counterService;

  /**
   *
   * @param filename
   * @param stream
   * @return
   */
  public Request parseRequestFromExcelSheet(String filename, InputStream stream) {
    RequestParser parser = RequestParserFactory.createRequestParser(stream);
    Request request = parser.parseRequest();

    request.setDescription(filename);
    request.setStatus(RequestStatus.IN_PROGRESS);

    // Link to the correct schema
//    Schema schema = schemaRepository.findOneByNameIgnoreCase(request.getDatasource().toLowerCase());
//    request.setSchema(schema);

    // Generate a request id
    request.setRequestId(counterService.getNextSequence("requests").toString());
    LOG.debug("generated request id: " + request.getRequestId());

    // Store the request in the database
    requestRepository.insert(request);
    return request;
  }
}
