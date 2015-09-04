package cern.modesti.worflow;

import cern.modesti.request.Request;
import cern.modesti.request.RequestStatus;
import cern.modesti.request.point.state.Addressing;
import cern.modesti.request.point.state.Approval;
import cern.modesti.request.point.state.Cabling;
import cern.modesti.request.point.state.Testing;
import cern.modesti.security.ldap.User;
import cern.modesti.util.BaseIntegrationTest;
import cern.modesti.workflow.task.TaskAction;
import cern.modesti.workflow.task.TaskInfo;
import cern.modesti.workflow.task.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static cern.modesti.util.TestUtil.*;
import static cern.modesti.workflow.task.TaskAction.Action.CLAIM;
import static cern.modesti.workflow.task.TaskAction.Action.COMPLETE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * TODO:
 *
 *  - Test request modification signal
 *  - Test split signal
 *  - Test CSAM workflow
 *  - Test WINCC workflow
 */
public class WorkflowServiceTest extends BaseIntegrationTest {

  @Autowired
  TaskService taskService;

  @Test
  public void createPoints() throws Exception {
    Request request = getDefaultRequest();
    requestRepository.save(request);
    ProcessInstance process = workflowService.startProcessInstance(request);
    // The business key should have been set to the request id.
    assertEquals(request.getRequestId(), process.getBusinessKey());

    // First, the 'edit' task should be active and the request should be 'IN_PROGRESS'.
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.IN_PROGRESS);

    // Completing the 'edit' task should validate the request and activate the 'submit' task
    // TODO need to mock out the validate task
    completeCurrentTask(request.getRequestId(), "edit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.IN_PROGRESS);

    // Completing the 'submit' task should activate the 'configure' task, since the basic test points are not alarms nor cabled points. The request status
    // should be 'FOR_CONFIGURATION'
    completeCurrentTask(request.getRequestId(), "submit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", RequestStatus.FOR_CONFIGURATION);

    // Stub the 'configure' service task, making sure the 'configured' execution variable is set.
    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    // Completing the 'configure' task should activate the 'test' task. The request status should be 'FOR_TESTING'.
    completeCurrentTask(request.getRequestId(), "configure", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    // Completing the 'test' task should close the request.
    completeCurrentTask(request.getRequestId(), "test", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), null, RequestStatus.CLOSED);

    // Verify process completed
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
    requestRepository.delete(request);
  }

  @Test
  public void createAlarms() throws Exception {
    Request request = getDefaultRequestWithAlarms();
    requestRepository.save(request);

    ProcessInstance process = workflowService.startProcessInstance(request);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.IN_PROGRESS);
    completeCurrentTask(request.getRequestId(), "edit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.IN_PROGRESS);
    // Completing the 'submit' task should activate the 'edit' task in the 'approval' subprocess, since we have alarms.
    completeCurrentTask(request.getRequestId(), "submit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.FOR_APPROVAL);

    // Verify emails were sent
    assertEquals(2, wiser.getMessages().size());

    // Claim the "edit" task as an approver
    claimCurrentTask(request.getRequestId(), "edit", DAN);

    // Set the approval result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setApproval(new Approval(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "edit", DAN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.FOR_APPROVAL);
    completeCurrentTask(request.getRequestId(), "submit", DAN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", RequestStatus.FOR_CONFIGURATION);

    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    completeCurrentTask(request.getRequestId(), "configure", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "test", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), null, RequestStatus.CLOSED);
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
    requestRepository.delete(request);
  }

  @Test
  public void createCabledPoints() throws Exception {
    Request request = getDefaultRequestWithCabledPoints();
    requestRepository.save(request);

    ProcessInstance process = workflowService.startProcessInstance(request);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.IN_PROGRESS);
    completeCurrentTask(request.getRequestId(), "edit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.IN_PROGRESS);
    // Completing the 'submit' task should activate the 'edit' task in the 'addressing' subprocess, since we have cabled points.
    completeCurrentTask(request.getRequestId(), "submit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.FOR_ADDRESSING);

    // Verify emails were sent
    assertEquals(2, wiser.getMessages().size());

    // Claim the "edit" task as an addresser
    claimCurrentTask(request.getRequestId(), "edit", ROB);

    // Set the addressing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setAddressing(new Addressing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "edit", ROB);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.FOR_ADDRESSING);

    // Completing the 'submit' task should lead to the 'cable' task
    completeCurrentTask(request.getRequestId(), "submit", ROB);
    assertTaskNameAndRequestStatus(request.getRequestId(), "cable", RequestStatus.FOR_CABLING);

    // Claim the "cable" task as a cabler
    claimCurrentTask(request.getRequestId(), "cable", ROB);

    // Set the cabling result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setCabling(new Cabling(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "cable", ROB);
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", RequestStatus.FOR_CONFIGURATION);

    // Verify emails were sent
    assertEquals(4, wiser.getMessages().size());

    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    completeCurrentTask(request.getRequestId(), "configure", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "test", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), null, RequestStatus.CLOSED);
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
    requestRepository.delete(request);
  }

  @Test
  public void createCabledAlarms() throws Exception {
    Request request = getDefaultRequestWithCabledAlarms();
    requestRepository.save(request);

    ProcessInstance process = workflowService.startProcessInstance(request);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.IN_PROGRESS);
    completeCurrentTask(request.getRequestId(), "edit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.IN_PROGRESS);
    // Completing the 'submit' task should activate the 'edit' task in the 'approval' subprocess, since we have cabled alarms.
    completeCurrentTask(request.getRequestId(), "submit", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.FOR_APPROVAL);

    // Verify emails were sent
    assertEquals(2, wiser.getMessages().size());

    // Claim the "edit" task as an approver
    claimCurrentTask(request.getRequestId(), "edit", DAN);

    // Set the approval result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setApproval(new Approval(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "edit", DAN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.FOR_APPROVAL);

    // Completing the 'submit' task should activate the 'edit' task in the 'addressing' subprocess, since we have cabled alarms.
    completeCurrentTask(request.getRequestId(), "submit", DAN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "edit", RequestStatus.FOR_ADDRESSING);

    // Verify emails were sent
    assertEquals(5, wiser.getMessages().size());

    // Claim the "edit" task as a cabler
    claimCurrentTask(request.getRequestId(), "edit", ROB);

    // Set the addressing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setAddressing(new Addressing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "edit", ROB);
    assertTaskNameAndRequestStatus(request.getRequestId(), "submit", RequestStatus.FOR_ADDRESSING);

    // Completing the 'submit' task should lead to the 'cable' task
    completeCurrentTask(request.getRequestId(), "submit", ROB);
    assertTaskNameAndRequestStatus(request.getRequestId(), "cable", RequestStatus.FOR_CABLING);

    // Verify emails were sent
    assertEquals(7, wiser.getMessages().size());

    // Claim the "cable" task as a cabler
    claimCurrentTask(request.getRequestId(), "cable", ROB);

    // Set the cabling result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setCabling(new Cabling(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "cable", ROB);
    assertTaskNameAndRequestStatus(request.getRequestId(), "configure", RequestStatus.FOR_CONFIGURATION);

    doAnswer(invocation -> {
      DelegateExecution execution = (DelegateExecution) invocation.getArguments()[1];
      execution.setVariable("configured", true);
      return null;
    }).when(configurationService).configureRequest(anyString(), anyObject());

    completeCurrentTask(request.getRequestId(), "configure", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), "test", RequestStatus.FOR_TESTING);

    // Set the testing result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setTesting(new Testing(true, ""));
    requestRepository.save(request);

    completeCurrentTask(request.getRequestId(), "test", BEN);
    assertTaskNameAndRequestStatus(request.getRequestId(), null, RequestStatus.CLOSED);
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    historyService.deleteHistoricProcessInstance(process.getProcessInstanceId());
    requestRepository.delete(request);
  }

  /**
   *
   * @param requestId
   * @param taskName
   * @param status
   */
  private void assertTaskNameAndRequestStatus(String requestId, String taskName, RequestStatus status) {
    if (taskName != null) {
      TaskInfo task = taskService.getTask(requestId, taskName);
      assertEquals(taskName, task.getName());
    }

    Request request = requestRepository.findOneByRequestId(requestId);
    assertEquals(status, request.getStatus());
  }

  /**
   *
   * @param requestId
   * @param taskName
   * @param user
   */
  private void claimCurrentTask(String requestId, String taskName, User user) {
    TaskInfo task = taskService.getTask(requestId, taskName);
    taskService.execute(requestId, task.getName(), new TaskAction(CLAIM, user.getUsername()), user);
  }

  /**
   *
   * @param requestId
   * @param taskName
   * @param user
   */
  private void completeCurrentTask(String requestId, String taskName, User user) {
    TaskInfo task = taskService.getTask(requestId, taskName);
    taskService.execute(requestId, task.getName(), new TaskAction(COMPLETE, user.getUsername()), user);
  }
}