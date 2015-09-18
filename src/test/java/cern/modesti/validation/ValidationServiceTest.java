package cern.modesti.validation;

import cern.modesti.Application;
import cern.modesti.repository.gmao.GmaoCode;
import cern.modesti.repository.location.Location;
import cern.modesti.repository.location.functionality.Functionality;
import cern.modesti.repository.person.Person;
import cern.modesti.repository.subsystem.SubSystem;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cern.modesti.request.RequestStatus.IN_PROGRESS;
import static cern.modesti.request.RequestType.CREATE;
import static cern.modesti.util.TestUtil.BEN;
import static org.junit.Assert.assertFalse;

/**
 * This class defines a set of unit tests to test the validations which are currently implemented as PL/SQL procedures on timrefdb. Hence, a real connection to
 * timrefdb is needed, and hence the "dev" profile is used.
 *
 * TODO write these tests once the validations have been implemented...
 *
 * @author Justin Lewis Salmon
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource("classpath:modesti-dev.properties")
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("dev")
public class ValidationServiceTest {

  @Autowired
  ValidationService validationService;

  @Test
  @Ignore
  public void invalidPlcAddress() {
    Request request = new Request();
    request.setRequestId("1");
    request.setCreator(BEN);
    request.setDomain("TIM");
    request.setStatus(IN_PROGRESS);
    request.setType(CREATE);

    List<Point> points = new ArrayList<>();
    Point point = new Point();

    Map<String, Object> properties = new HashMap<>();
    properties.put("pointDescription", "test");
    properties.put("pointDatatype", "Boolean");
    properties.put("gmaoCode", new GmaoCode("YCPLC01=LHC0"));
    properties.put("subsystem", new SubSystem(7092L, "ACLR GENERALE", "ACLR", "A", "GENERALE", "Z"));
    properties.put("responsiblePerson", new Person(413122L, "Robin Martini", "martini"));
    properties.put("pointAttribute", "test");
    properties.put("pointType", "APIMMD");
    properties.put("location", new Location("104/R-A01", "104", "R", "A01"));
    properties.put("functionality", new Functionality("ADE"));
    point.setProperties(properties);
    points.add(point);

    request.setPoints(points);

    boolean valid = validationService.validateRequest(request);
    assertFalse(valid);
  }
}
