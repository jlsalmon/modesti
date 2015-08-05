/**
 *
 */
package cern.modesti.upload;

import cern.modesti.upload.parser.RequestParser;
import cern.modesti.upload.parser.RequestParserFactory;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.request.Request;
import cern.modesti.security.ldap.User;
import cern.modesti.workflow.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.Principal;

/**
 * @author Justin Lewis Salmon
 *
 */
@Service
@Slf4j
public class UploadService {

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private RequestParserFactory requestParserFactory;

  @Autowired
  private CounterService counterService;

  @Autowired
  private WorkflowService workflowService;

  /**
   *
   * @param description
   * @param stream
   * @param principal
   * @return
   */
  public Request parseRequestFromExcelSheet(String description, InputStream stream, Principal principal) {
    RequestParser parser = requestParserFactory.createRequestParser(stream);
    Request request = parser.parseRequest();

    if (request.getDescription() == null) {
      request.setDescription(description);
    }

    request.setCreator((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());

    // Generate a request id
    request.setRequestId(counterService.getNextSequence("requests").toString());
    log.debug("generated request id: " + request.getRequestId());

    // Kick off the workflow process
    workflowService.startProcessInstance(request);

    // Store the request in the database
    requestRepository.save(request);
    return request;
  }
}
