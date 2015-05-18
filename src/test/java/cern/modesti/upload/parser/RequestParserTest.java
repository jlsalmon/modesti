/**
 *
 */
package cern.modesti.upload.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.modesti.request.RequestType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import cern.modesti.request.point.Point;
import cern.modesti.request.Request;
import cern.modesti.legacy.exception.RequestParseException;
import cern.modesti.legacy.exception.VersionNotSupportedException;
import cern.modesti.legacy.parser.RequestParserFactory;

/**
 * @author Justin Lewis Salmon
 */
public class RequestParserTest {

  /**
   * List of Excel sheets loaded from test resources
   */
  static Map<String, Resource> sheets = new HashMap<>();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource[] sheets = resolver.getResources("classpath:/sheets/**/*.xls*");

    for (Resource sheet : sheets) {
      RequestParserTest.sheets.put(sheet.getFilename(), sheet);
    }
  }

  @Test(expected = RequestParseException.class)
  public void invalidRequestDomainIsRejected() throws IOException {
    Resource sheet = sheets.get("invalid-domain.xlsx");
    RequestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  @Test(expected = RequestParseException.class)
  public void invalidRequestTypeIsRejected() throws IOException {
    Resource sheet = sheets.get("invalid-request-type.xlsx");
    RequestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  @Test(expected = VersionNotSupportedException.class)
  public void unsupportedExcelSheetVersionIsRejected() throws IOException {
    Resource sheet = sheets.get("unsupported-version.xlsx");
    RequestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  @Test(expected = RequestParseException.class)
  public void invalidFileTypeIsRejected() {
    InputStream stream = new ByteArrayInputStream("spam".getBytes());
    RequestParserFactory.createRequestParser(stream).parseRequest();
  }

  @Test(expected = RequestParseException.class)
  public void emptyExcelSheetIsRejected() throws IOException {
    Resource sheet = sheets.get("tim-empty.xls");
    RequestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  @Test
  public void timPlcRequestWithAlarmsIsAccepted() throws IOException {
    Resource sheet = sheets.get("tim-plc-with-alarms.xls");
    Request request = RequestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();

    assertTrue(request.getDomain().equals("TIM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getCategories().size() == 1);
    assertTrue(request.getCategories().get(0).equals("PLC"));

    List<Point> points = request.getPoints();
    assertTrue(points.size() == 279);

    for (Point point : points) {
      assertTrue(point.getId() != null);
      assertFalse(point.getProperties().isEmpty());
    }

  }
}
