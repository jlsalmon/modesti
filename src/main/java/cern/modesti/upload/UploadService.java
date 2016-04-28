package cern.modesti.upload;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.RequestService;
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
 * Service class for handling parsing stage of a {@link Request} upload.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class UploadService {

  @Autowired
  private RequestService requestService;

  @Autowired
  private RequestParserFactory requestParserFactory;

  /**
   * Delegate the parsing of the uploaded Excel sheet to a specific plugin
   * implementation.
   *
   * @param description the description of the uploaded request
   * @param stream      the Excel sheet to be parsed
   * @return the result of the request parse
   */
  public RequestParseResult parseRequestFromExcelSheet(String description, InputStream stream) {
    RequestParseResult result = requestParserFactory.parseRequest(stream);
    Request request = result.getRequest();

    if (request.getDescription() == null) {
      request.setDescription(description);
    }

    requestService.insert(request);

    return result;
  }
}
