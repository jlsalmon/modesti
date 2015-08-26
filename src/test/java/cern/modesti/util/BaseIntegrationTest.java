package cern.modesti.util;

import cern.modesti.Application;
import cern.modesti.configuration.ConfigurationService;
import cern.modesti.request.RequestRepository;
import cern.modesti.workflow.WorkflowService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.test.mock.MockExpressionManager;
import org.activiti.engine.test.mock.Mocks;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource("classpath:modesti-test.properties")
@WebIntegrationTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  @Autowired
  public RequestRepository requestRepository;

  @Autowired
  public RuntimeService runtimeService;

  @Autowired
  public org.activiti.engine.TaskService taskService;

  @Autowired
  public HistoryService historyService;

  @Autowired
  public WorkflowService workflowService;

  @Mock
  public ConfigurationService configurationService;

  /**
   * In-memory SMTP server for receiving emails sent during the tests.
   */
  public Wiser wiser;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    SpringProcessEngineConfiguration configuration = (SpringProcessEngineConfiguration) ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();
    configuration.setExpressionManager(new MockExpressionManager());

    Mocks.register("workflowService", workflowService);
    Mocks.register("configurationService", configurationService);

    wiser = new Wiser();
    wiser.setPort(25000);
    wiser.setHostname("localhost");
    wiser.start();
  }

  @After
  public void tearDown() throws Exception {
    wiser.stop();
  }
}
