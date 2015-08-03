package cern.modesti.configuration;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import cern.modesti.security.ldap.User;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceTest {

  @InjectMocks
  ConfigurationService configurationService;

  @Test
  public void test() {
    Request request = getTestRequest();
    ConfigurationReport report = configurationService.configureRequest(request, null);
  }

  private Request getTestRequest() {
    Request request = new Request();
    request.setRequestId("1234");
    request.setDescription("test request");
    request.setCreator(new User(1234, "nobody", "No", "Body", "nobody@cern.ch", Collections.emptySet()));

    Point p1 = new Point();
    p1.setId(1L);
    p1.setProperties(new HashMap<String, Object>() {{
      put("pointDescription", "test point 1");
      put("pointDatatype", "Boolean");
      put("monitoringEquipment", new HashMap<String, Object>() {{
        put("id", 500L);
      }});
    }});

    request.setPoints(Collections.singletonList(p1));
    return request;
  }
}
