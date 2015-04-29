/**
 *
 */
package cern.modesti.legacy.parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.request.point.Point;

import java.util.List;

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
  protected List<String> parseCategories(List<Point> points) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
