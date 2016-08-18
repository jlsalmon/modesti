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
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import java.io.InputStream;

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
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  /**
   * Parse the given Excel sheet as a {@link Request} instance.
   *
   * @param stream the Excel sheet input stream
   * @return the result of the parse operation
   */
  public RequestParseResult parseRequest(InputStream stream) {
    Sheet sheet = getSheet(stream);
    Row header = sheet.getRow(0);

    String domain = header.getCell(0).getStringCellValue().trim();

    // Search for a plugin which is capable of parsing this request.
    RequestParser parser = null;
    for (RequestProvider provider : requestProviderRegistry.getPlugins()) {
      String name = provider.getMetadata().getName();

      if (name.equals(domain)) {
        parser = provider.getRequestParser();
      }

      // FIXME: HACK ALERT: WINCCOA excel sheets use the old PVSS name...
      else if ((name.contains("WinCC OA") || name.contains("WINCCOA")) && domain.equals("PVSS")) {
        parser = provider.getRequestParser();
      }
    }

    if (parser == null) {
      throw new UnsupportedRequestException("No parser found for domain " + domain);
    }

    RequestParseResult result = parser.parseRequest(sheet);

    if (result.getRequest().getPoints().isEmpty()) {
      throw new RequestParseException("Sheet contains no data points");
    }

    return result;
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
