/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.ArrayList;
import java.util.List;

import cern.modesti.request.RequestType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.modesti.request.point.Point;
import cern.modesti.request.Request;
import cern.modesti.legacy.exception.RequestParseException;
import cern.modesti.legacy.exception.VersionNotSupportedException;

/**
 * @author Justin Lewis Salmon
 */
public abstract class RequestParser {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParser.class);

  private static final Double MINIMUM_SUPPORTED_VERSION = 4.2;

  protected static final int FIRST_DATA_ROW = 7;

  protected Sheet sheet;

  /**
   * @param sheet
   */
  public RequestParser(Sheet sheet) {
    this.sheet = sheet;
  }

  /**
   *
   * @return
   */
  public Request parseRequest() {
    Request request = new Request();

    request.setDomain(parseDomain());
    request.setType(parseRequestType());

    Double version = parseVersion();

    // Parse all the points from the request
    List<Point> points = parseDataPoints();
    if (points.isEmpty()) {
      throw new RequestParseException("Request contained no data points");
    }
    request.setPoints(points);

    // Figure out the data source
    request.setDatasource(parseDatasource(points));

    return request;
  }

  private String parseDomain() {
    return sheet.getRow(0).getCell(0).getStringCellValue().trim();
  }

  /**
   * Only versions greater than 4.2 are supported.
   *
   * @return
   */
  private Double parseVersion() {
    Double version = Double.valueOf(sheet.getRow(0).getCell(3).getStringCellValue());
    if (version < 4.2) {
      throw new VersionNotSupportedException("Legacy MODESTI Excel file version " + version + " not supported. Minimum supported version is " + MINIMUM_SUPPORTED_VERSION);
    }
    return version;
  }

  /**
   *
   * @return
   */
  private String parseRequestType() {
    String type = sheet.getRow(0).getCell(1).getStringCellValue();

    if (type.contains("creation")) {
      type = RequestType.CREATE.toString();
    } else if (type.contains("modification")) {
      type = RequestType.MODIFY.toString();
    } else if (type.contains("deletion")) {
      type = RequestType.DELETE.toString();
    } else {
      throw new RequestParseException("Invalid request type: " + type);
    }

    return type;
  }

  /**
   *
   * @return
   */
  private List<Point> parseDataPoints() {
    List<Point> points = new ArrayList<Point>();

    for (int i = FIRST_DATA_ROW; i < sheet.getLastRowNum(); i++) {
      Point point = parseDataPoint(sheet.getRow(i));

      // If we find an empty row, stop processing.
      if (point == null) {
        break;
      }

      points.add(point);
    }

    return points;
  }

  /**
   * @param row
   *
   * @return null if the row contained no data
   */
  protected abstract Point parseDataPoint(Row row);

  /**
   * @param points
   *
   * @return
   */
  protected abstract String parseDatasource(List<Point> points);
}
