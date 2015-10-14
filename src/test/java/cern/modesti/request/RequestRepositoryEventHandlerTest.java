/**
 *
 */
package cern.modesti.request;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.request.counter.CounterServiceImpl;
import cern.modesti.request.point.Point;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.workflow.CoreWorkflowService;
import org.apache.catalina.core.ApplicationContext;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.plugin.core.PluginRegistry;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Justin Lewis Salmon
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestRepositoryEventHandlerTest {

  /**
   * The class to be tested
   */
  @InjectMocks
  RequestRepositoryEventHandler requestRepositoryEventHandler;

  @Mock
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Mock
  CounterServiceImpl counterService;

  @Mock
  CoreWorkflowService workflowService;

  @Mock
  SchemaRepository schemaRepository;

  @Mock
  ApplicationContext applicationContext;

  @Test
  @Ignore
  public void requestIsCreatedInProgress() throws Exception {
    when(schemaRepository.findOne(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestRepositoryEventHandler.handleRequestCreate(request);
    assertTrue(Objects.equals(request.getStatus(), "IN_PROGRESS"));
  }

  @Test
  public void requestIdIsGenerated() {
    when(requestProviderRegistry.hasPluginFor(anyObject())).thenReturn(true);
    when(counterService.getNextSequence(anyString())).thenReturn(1L);
    when(schemaRepository.findOne(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestRepositoryEventHandler.handleRequestCreate(request);
    assertTrue(request.getRequestId().equals("1"));
  }

  @Test
  public void requestSaveGeneratesPointIds() {
    when(counterService.getNextSequence(anyString())).thenReturn(1L).thenReturn(2L);

    Request request = getTestRequest();
    requestRepositoryEventHandler.handleRequestSave(request);
    assertTrue(request.getPoints().get(0).getLineNo() == 1L);
    assertTrue(request.getPoints().get(1).getLineNo() == 2L);
  }

  private Request getTestRequest() {
    Request request = new Request();
    request.setType(RequestType.CREATE);
    request.setDescription("description");
    request.setDomain("TIM");
    request.setPoints(getTestPoints());
    return request;
  }

  private List<Point> getTestPoints() {
    List<Point> points = new ArrayList<>();
    points.add(new Point());
    points.add(new Point());
    return points;
  }
}
