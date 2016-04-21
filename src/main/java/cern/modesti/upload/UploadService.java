package cern.modesti.upload;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.counter.CounterService;
import cern.modesti.request.history.RequestHistoryService;
import cern.modesti.upload.parser.RequestParseResult;
import cern.modesti.upload.parser.RequestParserFactory;
import cern.modesti.user.User;
import cern.modesti.workflow.CoreWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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
  private RequestHistoryService historyService;

  @Autowired
  private CounterService counterService;

  @Autowired
  private CoreWorkflowService workflowService;

  /**
   *
   * @param description
   * @param stream
   * @param principal
   * @return
   */
  public RequestParseResult parseRequestFromExcelSheet(String description, InputStream stream, Principal principal) {
    RequestParseResult result = requestParserFactory.parseRequest(stream);
    Request request = result.getRequest();

    if (request.getDescription() == null) {
      request.setDescription(description);
    }

    request.setCreatedAt(new DateTime());
    request.setCreator(((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUsername());

    // Generate a request id
    request.setRequestId(counterService.getNextSequence("requests").toString());
    log.debug("generated request id: " + request.getRequestId());

    // Store the request in the database
    requestRepository.save(request);

    // Store an initial, empty change history
    historyService.initialiseChangeHistory(request);

    // Kick off the workflow process
    workflowService.startProcessInstance(request);

    return result;
  }
}
