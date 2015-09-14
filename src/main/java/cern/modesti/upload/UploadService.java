/**
 *
 */
package cern.modesti.upload;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.counter.CounterService;
import cern.modesti.upload.parser.RequestParser;
import cern.modesti.upload.parser.RequestParserFactory;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.Request;
import cern.modesti.security.ldap.User;
import cern.modesti.workflow.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
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
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

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

    // Do not create a request if there is no appropriate domain
    if (!requestProviderRegistry.hasPluginFor(request)) {
      throw new UnsupportedRequestException(request);
    }

    // Generate a request id
    request.setRequestId(counterService.getNextSequence("requests").toString());
    log.debug("generated request id: " + request.getRequestId());

    // Store the request in the database
    requestRepository.save(request);

    // Kick off the workflow process
    workflowService.startProcessInstance(request);

    return request;
  }
}
