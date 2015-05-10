/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.model.SubSystem;
import cern.modesti.request.point.Point;

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
  protected SubSystem parseSubsystem(List<Point> points) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected List<String> parseCategories(List<Point> points) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
