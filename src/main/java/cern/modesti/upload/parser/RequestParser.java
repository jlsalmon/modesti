package cern.modesti.upload.parser;

import cern.modesti.request.Request;
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
  Request parseRequest(Sheet sheet);
}
