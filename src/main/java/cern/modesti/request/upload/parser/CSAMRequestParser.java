/**
 *
 */
package cern.modesti.request.upload.parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.model.Point;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 *
 */
public class CSAMRequestParser extends RequestParser {

  /**
   * @param sheet
   */
  public CSAMRequestParser(Sheet sheet) {
    super(sheet);
  }

  @Override
  protected Point parseDataPoint(Row row) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected String parseDatasource(List<Point> points) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
