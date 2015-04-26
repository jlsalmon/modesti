/**
 *
 */
package cern.modesti.request.upload.parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.model.Point;

/**
 * @author Justin Lewis Salmon
 *
 */
public class PVSSRequestParser extends RequestParser {

  /**
   * @param sheet
   */
  public PVSSRequestParser(Sheet sheet) {
    super(sheet);
  }

  @Override
  protected Point parseDataPoint(Row row) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected String parseDatasource(Sheet sheet) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
