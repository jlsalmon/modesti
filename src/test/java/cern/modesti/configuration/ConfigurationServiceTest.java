package cern.modesti.configuration;

import cern.modesti.tim.configuration.TimConfigurationService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.modesti.request.Request;

import static cern.modesti.util.TestUtil.getTimRequest;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceTest {

  @InjectMocks
  TimConfigurationService configurationService;

  @Test
  @Ignore
  public void test() {
    Request request = getTimRequest();
    ConfigurationReport report = configurationService.configureRequest(request, null);
  }
}
