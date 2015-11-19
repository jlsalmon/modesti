package cern.modesti.upload.parser;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.plugin.UnsupportedRequestException;
import cern.modesti.request.Request;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.upload.exception.RequestParseException;
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
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class RequestParserFactory {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private SchemaRepository schemaRepository;

  /**
   * @param stream
   *
   * @return
   */
  public Request parseRequest(InputStream stream) {
    Sheet sheet = getSheet(stream);
    Row header = sheet.getRow(0);

    String domain = header.getCell(0).getStringCellValue().trim();

    // Search for a plugin which is capable of parsing this request.
    RequestParser parser = null;
    for (RequestProvider provider : requestProviderRegistry.getPlugins()) {
      if (provider.getMetadata().getName().equals(domain)) {
        parser = provider.getRequestParser();
      }

      // HACK: WINCC excel sheets use the old PVSS name...
      else if (provider.getMetadata().getName().equals("WinCC OA (CV)") && domain.equals("PVSS")) {
        parser = provider.getRequestParser();
      }
    }

    if (parser == null) {
      throw new UnsupportedRequestException("No parser found for domain " + domain);
    }

    Request request = parser.parseRequest(sheet);

    if (request.getPoints().isEmpty()) {
      throw new RequestParseException("Sheet contains no data points");
    }

    return request;
  }

  /**
   * Parse a {@link Sheet} from the given {@link InputStream}.
   *
   * @param stream
   *
   * @return
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
