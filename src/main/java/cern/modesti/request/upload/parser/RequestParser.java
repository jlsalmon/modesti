/**
 *
 */
package cern.modesti.request.upload.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.modesti.model.Point;
import cern.modesti.model.Request;
import cern.modesti.request.upload.exception.RequestParseException;
import cern.modesti.request.upload.exception.VersionNotSupportedException;

/**
 * @author Justin Lewis Salmon
 */
public abstract class RequestParser {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParser.class);

  protected static final int FIRST_DATA_ROW = 7;

  protected Sheet sheet;

  /**
   *
   * @param sheet
   */
  public RequestParser(Sheet sheet) {
    this.sheet = sheet;
  }

  /**
   *
   * @param stream
   * @return
   */
  public Request parseRequest() {
    Request request = new Request();
    Row header = sheet.getRow(0);

    String domain = header.getCell(0).getStringCellValue().trim();
    LOG.info("domain: " + domain);
    request.setDomain(domain);

    String type = parseRequestType(header.getCell(1).getStringCellValue());
    LOG.info("type: " + type);
    request.setType(type);

    // Only versions greater than 4.2 are supported
    Double version = Double.valueOf(header.getCell(3).getStringCellValue());
    if (version < 4.2) {
      throw new VersionNotSupportedException("Legacy MODESTI Excel file version " + version + " not supported");
    }

    // Parse all the points from the request
    List<Point> points = parseDataPoints(sheet);
    if (points.isEmpty()) {
      throw new RequestParseException("Request contained no data points");
    }
    request.setPoints(points);

    // Figure out the data source
    String datasource = parseDatasource(points, sheet);
    LOG.info("datasource: " + datasource);
    request.setDatasource(datasource);

    return request;
  }

  /**
   * @param sheet
   * @return
   */
  private List<Point> parseDataPoints(Sheet sheet) {
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
   *
   * @param row
   * @return null if the row contained no data
   */
  protected abstract Point parseDataPoint(Row row);

  /**
   * @param points
   * @param sheet
   * @return
   */
  protected abstract String parseDatasource(List<Point> points, Sheet sheet);

  /**
   *
   * @param type
   * @return
   */
  private String parseRequestType(String type) {
    if (type.contains("creation")) {
      type = "create";
    } else if (type.contains("modification")) {
      type = "modify";
    } else if (type.contains("deletion")) {
      type = "delete";
    } else {
      throw new RequestParseException("Invalid request type: " + type);
    }

    return type;
  }
}
