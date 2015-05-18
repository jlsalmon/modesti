/**
 *
 */
package cern.modesti.legacy;

import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import cern.modesti.workflow.WorkflowService;
import org.activiti.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.legacy.parser.RequestParser;
import cern.modesti.legacy.parser.RequestParserFactory;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.request.Request;
import cern.modesti.request.Request.RequestStatus;

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
  private CounterService counterService;

  @Autowired
  private WorkflowService workflowService;

  /**
   *
   * @param filename
   * @param stream
   * @param user
   * @return
   */
  public Request parseRequestFromExcelSheet(String filename, InputStream stream, Principal user) {
    RequestParser parser = RequestParserFactory.createRequestParser(stream);
    Request request = parser.parseRequest();

    request.setDescription(filename);
    request.setCreator(user.getName());

    // Generate a request id
    request.setRequestId(counterService.getNextSequence("requests").toString());
    LOG.debug("generated request id: " + request.getRequestId());

    // Kick off the workflow process
    workflowService.startProcessInstance(request);

    // Store the request in the database
    requestRepository.insert(request);
    return request;
  }
}
