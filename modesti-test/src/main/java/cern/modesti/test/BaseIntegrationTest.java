package cern.modesti.test;

import cern.modesti.ModestiServer;
import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import cern.modesti.security.mock.MockUserService;
import cern.modesti.user.User;
import cern.modesti.workflow.CoreWorkflowService;
import cern.modesti.workflow.task.TaskAction;
import cern.modesti.workflow.task.TaskInfo;
import cern.modesti.workflow.task.TaskService;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.test.mock.MockExpressionManager;
import org.activiti.engine.test.mock.Mocks;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

import static cern.modesti.workflow.task.TaskAction.Action.ASSIGN;
import static cern.modesti.workflow.task.TaskAction.Action.COMPLETE;
import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ModestiServer.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = {
    "classpath:modesti-test.properties",
})
@ActiveProfiles("dev")
@ContextConfiguration
public abstract class BaseIntegrationTest {
  @Autowired
  public RequestService requestService;

  @Autowired
  public RuntimeService runtimeService;

  @Autowired
  public TaskService taskService;

  @Autowired
  public HistoryService historyService;

  @Autowired
  public CoreWorkflowService coreWorkflowService;

  @Autowired
  public MockUserService userService;
  
  /**
   * In-memory SMTP server for receiving emails sent during the tests.
   */
  @Autowired
  public Wiser emails;
  
  @Before
  public void setUpBase() throws Exception {
    MockitoAnnotations.initMocks(this);

    // Need to register a {@link MockExpressionManager} in order for Activiti to use mocks
    SpringProcessEngineConfiguration configuration = (SpringProcessEngineConfiguration) ProcessEngines.getDefaultProcessEngine().getProcessEngineConfiguration();
    configuration.setExpressionManager(new MockExpressionManager());

    Mocks.register("coreWorkflowService", coreWorkflowService);
  }
  
  public void assertTaskNameAndRequestStatus(String requestId, String taskName, String status) {
    TaskInfo task = taskService.getActiveTask(requestId);
    assertNotNull(task);
    assertEquals(taskName, task.getName());

    Request request = requestService.findOneByRequestId(requestId);
    assertEquals(status, request.getStatus());
  }

  public void claimCurrentTask(String requestId) {
    doAction(requestId, taskService.getActiveTask(requestId), ASSIGN);
  }

  public void completeCurrentTask(String requestId) {
    doAction(requestId, taskService.getActiveTask(requestId), COMPLETE);
  }

  public void doAction(String requestId, TaskInfo task, TaskAction.Action action) {
    assertNotNull(task);
    User user = userService.getCurrentUser();
    taskService.execute(requestId, task.getName(), new TaskAction(action, user.getUsername()), user);
  }

  public void assertWorkflowFinished(String requestId) {
    assertNull(taskService.getActiveTask(requestId));
    assertEquals("CLOSED", requestService.findOneByRequestId(requestId).getStatus());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  
  public void assertWorkflowFinishedWithRestFailedStatus(String requestId) {
    assertNull(taskService.getActiveTask(requestId));
    assertEquals("REST_FAILED", requestService.findOneByRequestId(requestId).getStatus());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
}
