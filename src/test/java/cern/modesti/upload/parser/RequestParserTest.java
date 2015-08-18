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

import cern.modesti.repository.jpa.equipment.MonitoringEquipmentRepository;
import cern.modesti.repository.jpa.location.functionality.FunctionalityRepository;
import cern.modesti.repository.jpa.location.zone.SafetyZoneRepository;
import cern.modesti.repository.jpa.person.PersonRepository;
import cern.modesti.repository.jpa.subsystem.SubSystemRepository;
import cern.modesti.request.RequestType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import cern.modesti.request.point.Point;
import cern.modesti.request.Request;
import cern.modesti.upload.exception.RequestParseException;
import cern.modesti.upload.exception.VersionNotSupportedException;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestParserTest {

  /**
   * List of Excel sheets loaded from test resources
   */
  static Map<String, Resource> sheets = new HashMap<>();

  @InjectMocks
  RequestParserFactory requestParserFactory;

  @Mock
  private PersonRepository personRepository;

  @Mock
  private SubSystemRepository subSystemRepository;

  @Mock
  private FunctionalityRepository functionalityRepository;

  @Mock
  private SafetyZoneRepository safetyZoneRepository;

  @Mock
  private MonitoringEquipmentRepository monitoringEquipmentRepository;

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
    requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  @Test(expected = RequestParseException.class)
  public void invalidRequestTypeIsRejected() throws IOException {
    Resource sheet = sheets.get("invalid-request-type.xlsx");
    requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  @Test(expected = VersionNotSupportedException.class)
  public void unsupportedExcelSheetVersionIsRejected() throws IOException {
    Resource sheet = sheets.get("unsupported-version.xlsx");
    requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  @Test(expected = RequestParseException.class)
  public void invalidFileTypeIsRejected() {
    InputStream stream = new ByteArrayInputStream("spam".getBytes());
    requestParserFactory.createRequestParser(stream).parseRequest();
  }

  @Test(expected = RequestParseException.class)
  public void emptyExcelSheetIsRejected() throws IOException {
    Resource sheet = sheets.get("tim-empty.xls");
    requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
  }

  /**
   * Tests that version 4.1 of a legacy TIM request is correctly parsed
   */
  @Test
  public void tim41isParsedCorrectly() throws IOException {
    Resource sheet = sheets.get("tim-4.1.xls");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
    assertTrue(request.getDomain().equals("TIM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getPoints().size() == 1);

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();

      assertCoreFieldsExist(properties);
      assertTimFieldsExist(properties);
    }
  }

  /**
   * Tests that version 4.2 of a legacy TIM request is correctly parsed
   */
  @Test
  public void tim42isParsedCorrectly() throws IOException {
    Resource sheet = sheets.get("tim-4.2.xls");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
    assertTrue(request.getDomain().equals("TIM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getPoints().size() == 1);

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();
      assertCoreFieldsExist(properties);
      assertTimFieldsExist(properties);

      // TODO Version 4.2 added DB DAQ field
      assertTrue(properties.get("dbTagname") != null);
    }
  }

  /**
   * Tests that version 6.1 of a legacy TIM request is correctly parsed
   */
  @Test
  public void tim61isParsedCorrectly() throws IOException {
    Resource sheet = sheets.get("tim-6.1.xls");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
    assertTrue(request.getDomain().equals("TIM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getPoints().size() == 1);

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();
      assertCoreFieldsExist(properties);
      assertTimFieldsExist(properties);

      // Version 6.1 added ZONE field
      assertTrue(properties.get("functionality") != null);
      // 6.1 added 4 OPC fields
      assertTrue(properties.get("opcAddressType") != null);
      assertTrue(properties.get("opcNamespace") != null);
      assertTrue(properties.get("opcCommandType") != null);
      assertTrue(properties.get("opcSampleRate") != null);
    }
  }

  /**
   * Tests that version 5.2 of a legacy CSAM request is correctly parsed
   */
  @Test
  public void csam52isParsedCorrectly() throws IOException {
    Resource sheet = sheets.get("csam-5.2.xls");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
    assertTrue(request.getDomain().equals("CSAM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getPoints().size() == 1);

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();
      assertCoreFieldsExist(properties);
      assertCsamFieldsExist(properties);
    }
  }

  /**
   * Tests that version 6.1 of a legacy CSAM request is correctly parsed
   */
  @Test
  public void csam61isParsedCorrectly() throws IOException {
    Resource sheet = sheets.get("csam-6.1.xlsx");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
    assertTrue(request.getDomain().equals("CSAM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getPoints().size() == 1);

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();
      assertCoreFieldsExist(properties);
      assertCsamFieldsExist(properties);

      // Version 6.1 added SECURIFIRE Type field
      assertTrue(properties.get("securifireType") != null);
    }
  }

  /**
   * Tests that version 1.1 of a legacy PVSS request is correctly parsed
   */
  @Test
  public void pvss11isParsedCorrectly() throws IOException {
    Resource sheet = sheets.get("pvss-1.1.xlsx");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();
    assertTrue(request.getDomain().equals("PVSS"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getPoints().size() == 1);

    for (Point point : request.getPoints()) {
      Map<String, Object> properties = point.getProperties();
      assertCoreFieldsExist(properties);
      assertPvssFieldsExist(properties);
    }
  }

  private void assertCoreFieldsExist(Map<String, Object> properties) {
    // General
    assertTrue(properties.get("pointDescription") != null);
    assertTrue(properties.get("pointDatatype") != null);
    assertTrue(properties.get("subsystem") != null);
    assertTrue(properties.get("responsiblePerson") != null);
    assertTrue(properties.get("pointAttribute") != null);
    // Location
    assertTrue(properties.get("functionality") != null);
    assertTrue(properties.get("location") != null);
    assertTrue(properties.get("buildingName") != null);
    // Alarms
    assertTrue(properties.get("priorityCode") != null);
    assertTrue(properties.get("alarmValue") != null);
    // Alarm Help
    assertTrue(properties.get("workHoursTask") != null);
    assertTrue(properties.get("outsideHoursTask") != null);
    // Analogue
    assertTrue(properties.get("lowLimit") != null);
    assertTrue(properties.get("highLimit") != null);
    assertTrue(properties.get("valueDeadband") != null);
    assertTrue(properties.get("units") != null);
  }

  private void assertTimFieldsExist(Map<String, Object> properties) {
    // General
    assertTrue(properties.get("gmaoCode") != null);
    assertTrue(properties.get("otherEquipCode") != null);
    assertTrue(properties.get("timeDeadband") != null);
    assertTrue(properties.get("pointCompInfo") != null);
    assertTrue(properties.get("userApplicationData") != null);
    // Location
    assertTrue(properties.get("detail") != null);
    // Alarms
    assertTrue(properties.get("alarmCategory") != null);
    assertTrue(properties.get("autocallNumber") != null);
    assertTrue(properties.get("multiplicityValue") != null);
    assertTrue(properties.get("parentAlarm") != null);
    assertTrue(properties.get("electricityFaultFamily") != null);
    // Alarm Help
    assertTrue(properties.get("alarmCauses") != null);
    assertTrue(properties.get("alarmConseq") != null);
    // DIP Client / JAPC Client
    assertTrue(properties.get("dipClientApp") != null);
    assertTrue(properties.get("dipRequestorId") != null);
    assertTrue(properties.get("japcClientApp") != null);
    assertTrue(properties.get("japcRequestorId") != null);
    // Binary Points
    assertTrue(properties.get("trueMeaning") != null);
    assertTrue(properties.get("falseMeaning") != null);
    // Commands
    assertTrue(properties.get("commandType") != null);
    assertTrue(properties.get("commandPulseLength") != null);
    // Monitoring
    assertTrue(properties.get("monitoringEquipment") != null);
    assertTrue(properties.get("cabling") != null);
    // OPC
    assertTrue(properties.get("opcTagname") != null);
    assertTrue(properties.get("opcTagtype") != null);
    assertTrue(properties.get("opcRedundantAddress") != null);
    // DIP
    assertTrue(properties.get("dipItem") != null);
    assertTrue(properties.get("dipField") != null);
    assertTrue(properties.get("dipIndex") != null);
    // PLC
    assertTrue(properties.get("plcBlockType") != null);
    assertTrue(properties.get("plcWordId") != null);
    assertTrue(properties.get("plcBitId") != null);
    assertTrue(properties.get("plcNativePrefix") != null);
    assertTrue(properties.get("plcSlaveAddress") != null);
    assertTrue(properties.get("plcConnectId") != null);
    // LASER
    assertTrue(properties.get("laserCategory") != null);
    assertTrue(properties.get("laserFaultFamily") != null);
    assertTrue(properties.get("laserFaultMember") != null);
    assertTrue(properties.get("laserFaultCode") != null);
    // JAPC
    assertTrue(properties.get("japcProtocol") != null);
    assertTrue(properties.get("japcService") != null);
    assertTrue(properties.get("japcDeviceName") != null);
    assertTrue(properties.get("japcPropertyName") != null);
    assertTrue(properties.get("japcIndexFieldName") != null);
    assertTrue(properties.get("japcIndexName") != null);
    assertTrue(properties.get("japcDataFieldName") != null);
    assertTrue(properties.get("japcColumnIndex") != null);
    assertTrue(properties.get("japcRowIndex") != null);
    // DIAMON
    assertTrue(properties.get("hostName") != null);
    // Analogue
    assertTrue(properties.get("deadbandType") != null);
    assertTrue(properties.get("plcAdConversion") != null);
    // Logging
    assertTrue(properties.get("logValueDeadband") != null);
    assertTrue(properties.get("logDeadbandType") != null);
    assertTrue(properties.get("logTimeDeadband") != null);
  }

  private void assertCsamFieldsExist(Map<String, Object> properties) {
    // General
    assertTrue(properties.get("csamCsename") != null);
    assertTrue(properties.get("csamDetector") != null);
    // Monitoring
    assertTrue(properties.get("csamPlcname") != null);
    // LSAC
    assertTrue(properties.get("lsacType") != null);
    assertTrue(properties.get("lsacRack") != null);
    assertTrue(properties.get("lsacCard") != null);
    assertTrue(properties.get("lsacPort") != null);
    // APIMMD
    assertTrue(properties.get("plcBlockType") != null);
    assertTrue(properties.get("plcWordId") != null);
    assertTrue(properties.get("plcBitId") != null);
    assertTrue(properties.get("plcSlaveAddress") != null);
    assertTrue(properties.get("plcNativePrefix") != null);
    assertTrue(properties.get("plcConnectId") != null);
    // PLC - OPC
    assertTrue(properties.get("safeplcByteId") != null);
    assertTrue(properties.get("safeplcBitId") != null);
    // WINTER
    assertTrue(properties.get("winterChannel") != null);
    assertTrue(properties.get("winterBit") != null);
    // SECURITON
    assertTrue(properties.get("securitonArea") != null);
    assertTrue(properties.get("securitonGroup") != null);
    assertTrue(properties.get("securitonDetecteur") != null);
    assertTrue(properties.get("securitonStatus") != null);
    assertTrue(properties.get("securitonMcu") != null);
    // SECURIFIRE
    assertTrue(properties.get("securifireGroup") != null);
    assertTrue(properties.get("securifireDetecteur") != null);
    assertTrue(properties.get("securifireStatus") != null);
    // OPCDEF
    assertTrue(properties.get("safedefModule") != null);
    assertTrue(properties.get("safedefLine") != null);
    assertTrue(properties.get("safedefAddress") != null);
    assertTrue(properties.get("safedefStatus") != null);
  }

  private void assertPvssFieldsExist(Map<String, Object> properties) {
    // General
    assertTrue(properties.get("gmaoCode") != null);
    assertTrue(properties.get("otherEquipCode") != null);
    assertTrue(properties.get("pvssReferenceId") != null);
    assertTrue(properties.get("laserSource") != null);
    assertTrue(properties.get("autocallNumber") != null);
    assertTrue(properties.get("alarmCategory") != null);
    // Alarms
    assertTrue(properties.get("pointCompInfo") != null);
    // Alarm Help
    assertTrue(properties.get("alarmCauses") != null);
    assertTrue(properties.get("alarmConseq") != null);
    // Binary Points
    assertTrue(properties.get("trueMeaning") != null);
    assertTrue(properties.get("falseMeaning") != null);
    // Analogue
    assertTrue(properties.get("deadbandType") != null);
    // JAPC
    // Binary Points
    assertTrue(properties.get("japcDevice") != null);
    assertTrue(properties.get("japcProperty") != null);
  }

  @Test
  public void timPlcRequestWithAlarmsIsAccepted() throws IOException {
    Resource sheet = sheets.get("tim-plc-with-alarms.xls");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();

    assertTrue(request.getDomain().equals("TIM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getCategories().size() == 3);
    assertTrue(request.getCategories().contains("PLC"));
    assertTrue(request.getCategories().contains("Commands"));
    assertTrue(request.getCategories().contains("Binary Points"));

    List<Point> points = request.getPoints();
    assertTrue(points.size() == 279);

    for (Point point : points) {
      assertTrue(point.getId() != null);
      assertFalse(point.getProperties().isEmpty());
    }
  }

  @Test
  public void csamPlcLsacRequestWithAlarmsIsAccepted() throws IOException {
    Resource sheet = sheets.get("csam-plc-lsac-alarms.xlsx");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();

    assertTrue(request.getDomain().equals("CSAM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    List<Point> points = request.getPoints();
    assertTrue(points.size() == 14);

    for (Point point : points) {
      assertTrue(point.getId() != null);
      assertFalse(point.getProperties().isEmpty());
    }
  }

  @Test
  public void csamPlcLsacRequestWithAddressedAlarmsIsAccepted() throws IOException {
    Resource sheet = sheets.get("csam-plc-lsac-alarms-addressed.xlsx");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();

    assertTrue(request.getDomain().equals("CSAM"));
    assertTrue(request.getType().equals(RequestType.CREATE));
    assertTrue(request.getCategories().size() == 2); // has PLC and LSAC points
    assertTrue(request.getCategories().contains("LSAC"));
    assertTrue(request.getCategories().contains("APIMMD"));

    List<Point> points = request.getPoints();
    assertTrue(points.size() == 14);

    for (Point point : points) {
      assertTrue(point.getId() != null);
      assertFalse(point.getProperties().isEmpty());
    }
  }

  @Test
  public void pvssRequestWithAlarmsIsAccepted() throws IOException {
    Resource sheet = sheets.get("pvss-alarms.xlsx");
    Request request = requestParserFactory.createRequestParser(sheet.getInputStream()).parseRequest();

    assertTrue(request.getDomain().equals("PVSS"));
    assertTrue(request.getType().equals(RequestType.CREATE));

    List<Point> points = request.getPoints();
    assertTrue(points.size() == 43);

    for (Point point : points) {
      assertTrue(point.getId() != null);
      assertFalse(point.getProperties().isEmpty());
    }
  }
}
