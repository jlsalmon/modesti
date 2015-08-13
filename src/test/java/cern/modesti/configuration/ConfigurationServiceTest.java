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

import static cern.modesti.util.TestUtil.getTestRequest;

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
}
