package cern.modesti.request.upload;

import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import cern.modesti.request.upload.parser.RequestParseResult;
import cern.modesti.request.upload.parser.RequestParserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Service class for handling parsing stage of a {@link Request} upload.
 *
 * @author Justin Lewis Salmon
 */
@Service
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
    request.setGeneratedFromUi(true);

    if (request.getDescription() == null) {
      request.setDescription(description);
    }

    requestService.insert(request);

    return result;
  }
}
