package cern.modesti.request.upload.parser;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.Request;
import cern.modesti.request.upload.exception.RequestParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import javax.annotation.PostConstruct;

/**
 * This class is responsible for delegating the parsing of a request to a
 * specific {@link RequestProvider} implementation.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class RequestParserFactory {

  @Autowired
  private ApplicationContext context;

  /**
   * Parse the given Excel sheet as a {@link Request} instance.
   *
   * @param stream the Excel sheet input stream
   * @return the result of the parse operation
   */
  public RequestParseResult parseRequest(InputStream stream) {
    Sheet sheet = getSheet(stream);
    Row header = sheet.getRow(0);

    String pluginId = header.getCell(0).getStringCellValue().trim();

    RequestParser parser = getPluginRequestParser(pluginId);

    if (parser == null) {
      throw new UnsupportedRequestException("No parser found for domain " + pluginId);
    }

    RequestParseResult result = parser.parseRequest(sheet);

    if (result.getRequest().getPoints().isEmpty()) {
      throw new RequestParseException("Sheet contains no data points");
    }

    return result;
  }

  private RequestParser getPluginRequestParser(String pluginId) {
    for (RequestParser requestParser : context.getBeansOfType(RequestParser.class).values()) {
      if (requestParser.getPluginId().equals(pluginId)) {
        return requestParser;
      }
    }
    return null;
  }

  /**
   * Parse a {@link Sheet} from the given {@link InputStream}.
   *
   * @param stream the Excel sheet input stream
   * @return a {@link Sheet} instance created from the input stream
   */
  private Sheet getSheet(InputStream stream) {
    Workbook workbook;

    try {
      workbook = WorkbookFactory.create(stream);
    } catch (Exception e) {
      log.error("Exception caught while creating request parser", e);
      throw new RequestParseException(e);
    }

    return workbook.getSheetAt(0);
  }
}
