package cern.modesti.request.upload.parser;

import cern.modesti.plugin.spi.ExtensionPoint;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * SPI for plugins that wish to provide upload functionality from Excel sheets
 * for {@link cern.modesti.request.Request} instances of their domain.
 *
 * @author Justin Lewis Salmon
 */
public interface RequestParser extends ExtensionPoint {

  /**
   * Parse a {@link cern.modesti.request.Request} instance from the given Excel
   * sheet.
   * <p>
   * It is the responsibility of the parser implementation to know the internal
   * structure of the Excel sheet.
   *
   * @param sheet the Excel sheet to parse
   * @return the result of the parse operation
   */
  RequestParseResult parseRequest(Sheet sheet);
}
