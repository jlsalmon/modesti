package cern.modesti.worflow;

import cern.modesti.Application;
import cern.modesti.configuration.ConfigurationService;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import cern.modesti.request.point.state.Addressing;
import cern.modesti.request.point.state.Approval;
import cern.modesti.request.point.state.Cabling;
import cern.modesti.request.point.state.Testing;
import cern.modesti.workflow.WorkflowService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.mock.MockExpressionManager;
import org.activiti.engine.test.mock.Mocks;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cern.modesti.util.TestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource("classpath:modesti-test.properties")
@WebIntegrationTest
public class WorkflowServiceTest {

  @Autowired
  RequestRepository requestRepository;

  @Autowired
  RuntimeService runtimeService;

  @Autowired
  TaskService taskService;

  @Autowired
  HistoryService historyService;

  @Autowired
  WorkflowService workflowService;

  @Mock
  ConfigurationService configurationService;

  /**
   * In-memory SMTP server for receiving emails sent during the tests.
   */
  Wiser wiser;

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

  @Test
  public void timPoints() throws Exception {
    Request request = getTimRequest();
    requestRepository.save(request);
    ProcessInstance process = startProcessInstance("create-tim-points-0.2", request);

    // First, the 'edit' task should be active and the request should be 'IN_PROGRESS'.
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.IN_PROGRESS);

    // Completing the 'edit' task should validate the request and activate the 'submit' task
    // TODO need to mock out the validate task
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.IN_PROGRESS);

    // Completing the 'submit' task should activate the 'configure' task, since the basic test points are not alarms nor cabled points. The request status
    // should be 'FOR_CONFIGURATION'
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", Request.RequestStatus.FOR_CONFIGURATION);

    // Stub the 'configure' service task, making sure the 'configured' execution variable is set.
    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    // Completing the 'configure' task should activate the 'test' task. The request status should be 'FOR_TESTING'.
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", Request.RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    // Completing the 'test' task should close the request.
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), null, Request.RequestStatus.CLOSED);

    // Verify process completed
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
  }

  @Test
  public void timAlarms() throws Exception {
    Request request = getTimRequestWithAlarms();
    requestRepository.save(request);

    ProcessInstance process = startProcessInstance("create-tim-points-0.2", request);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.IN_PROGRESS);
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.IN_PROGRESS);
    // Completing the 'submit' task should activate the 'edit' task in the 'approval' subprocess, since we have alarms.
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.FOR_APPROVAL);

    // TODO maybe test task claiming here? The test will work without, as we don't do any authorisation in this test atm.

    // Verify emails were sent
    assertEquals(2, wiser.getMessages().size());

    // Set the approval result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setApproval(new Approval(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.FOR_APPROVAL);
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", Request.RequestStatus.FOR_CONFIGURATION);

    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", Request.RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), null, Request.RequestStatus.CLOSED);
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
  }

  @Test
  public void timCabledPoints() throws Exception {
    Request request = getTimRequestWithCabledPoints();
    requestRepository.save(request);

    ProcessInstance process = startProcessInstance("create-tim-points-0.2", request);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.IN_PROGRESS);
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.IN_PROGRESS);
    // Completing the 'submit' task should activate the 'edit' task in the 'addressing' subprocess, since we have cabled points.
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.FOR_ADDRESSING);

    // Verify emails were sent
    assertEquals(2, wiser.getMessages().size());

    // Set the addressing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setAddressing(new Addressing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.FOR_ADDRESSING);

    // Completing the 'submit' task should lead to the 'cable' task
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "cable", Request.RequestStatus.FOR_CABLING);

    // Set the cabling result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setCabling(new Cabling(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", Request.RequestStatus.FOR_CONFIGURATION);

    // Verify emails were sent
    assertEquals(4, wiser.getMessages().size());

    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", Request.RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), null, Request.RequestStatus.CLOSED);
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
  }

  @Test
  public void timCabledAlarms() throws Exception {
    Request request = getTimRequestWithCabledAlarms();
    requestRepository.save(request);

    ProcessInstance process = startProcessInstance("create-tim-points-0.2", request);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.IN_PROGRESS);
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.IN_PROGRESS);
    // Completing the 'submit' task should activate the 'edit' task in the 'addressing' subprocess, since we have cabled alarms.
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.FOR_APPROVAL);

    // Verify emails were sent
    assertEquals(2, wiser.getMessages().size());

    // Set the approval result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setApproval(new Approval(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.FOR_APPROVAL);
    // Completing the 'submit' task should activate the 'edit' task in the 'addressing' subprocess, since we have cabled points.
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", Request.RequestStatus.FOR_ADDRESSING);

    // Verify emails were sent
    assertEquals(5, wiser.getMessages().size());

    // Set the addressing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setAddressing(new Addressing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", Request.RequestStatus.FOR_ADDRESSING);

    // Completing the 'submit' task should lead to the 'cable' task
    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "cable", Request.RequestStatus.FOR_CABLING);

    // Set the cabling result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setCabling(new Cabling(true, ""));
    requestRepository.save(request);

    // Verify emails were sent
    assertEquals(7, wiser.getMessages().size());

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", Request.RequestStatus.FOR_CONFIGURATION);

    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", Request.RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId());
    assertTaskNameAndRequestStatus(request.getRequestId(), null, Request.RequestStatus.CLOSED);
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
  }



  /**
   *
   * @param processName
   * @param request
   */
  private ProcessInstance startProcessInstance(String processName, Request request) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processName, variables);
    // The business key should have been set to the request id.
    assertEquals(request.getRequestId(), processInstance.getBusinessKey());
    return processInstance;
  }

  /**
   *
   * @param requestId
   * @param taskName
   * @param status
   */
  private void assertTaskNameAndRequestStatus(String requestId, String taskName, Request.RequestStatus status) {
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).active().singleResult();
    Request request = requestRepository.findOneByRequestId(requestId);

    if (taskName == null) assertNull(task); else assertEquals(taskName, task.getName());
    assertEquals(status, request.getStatus());
  }

  /**
   *
   * @param requestId
   */
  private void completeCurrentTask(String requestId) {
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(requestId).active().singleResult();
    taskService.complete(task.getId());
  }
}