package cern.modesti.upload.parser;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface RequestParser {

  /**
   *
   * @param sheet
   * @return
   */
  RequestParseResult parseRequest(Sheet sheet);
}
