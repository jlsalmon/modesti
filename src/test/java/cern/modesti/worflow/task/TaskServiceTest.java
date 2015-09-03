package cern.modesti.worflow.task;

import cern.modesti.request.Request;
import cern.modesti.point.state.Approval;
import cern.modesti.util.BaseIntegrationTest;
import cern.modesti.workflow.task.TaskAction;
import cern.modesti.workflow.task.TaskInfo;
import cern.modesti.workflow.task.TaskService;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static cern.modesti.util.TestUtil.*;
import static cern.modesti.workflow.task.TaskAction.Action.*;
import static org.junit.Assert.*;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class TaskServiceTest extends BaseIntegrationTest {

  @Autowired
  TaskService taskService;

  Request request;
  ProcessInstance process;

  @Before
  public void setUp() {
    request = getDefaultRequestWithAlarms();
    process = workflowService.startProcessInstance(request);
  }

  @After
  public void tearDown() {
    runtimeService.deleteProcessInstance(process.getProcessInstanceId(), null);
    requestRepository.delete(request.getRequestId());
  }

  @Test
  public void delegate() {
    TaskInfo task = taskService.getTask(request.getRequestId(), "edit");
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    //assertTrue(task.getAssignee().equals(BEN.getUsername()));

    // Ben delegates the task to Joe
    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(DELEGATE, JOE.getUsername()), BEN);
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    assertTrue(task.getAssignee().equals(JOE.getUsername()));
    assertTrue(task.getDelegationState().equals(DelegationState.PENDING));

    try {
      // Joe should NOT be able to complete the task, only resolve it back to Ben
      taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), JOE);
      fail("Delegates should not be able to complete tasks");
    } catch (ActivitiException ignored) {
    }

    // Joe resolves the task back to Ben
    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(RESOLVE, null), JOE);
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    assertTrue(task.getAssignee().equals(BEN.getUsername()));
    assertTrue(task.getDelegationState().equals(DelegationState.RESOLVED));

    // Ben may now complete the task
    taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), BEN); // activates "submit" task
    task = taskService.getTask(request.getRequestId(), "submit");

    taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), BEN); // activates "edit" task of "approval" subprocess
    task = taskService.getTask(request.getRequestId(), "edit");
    assertNull(task.getOwner());
    assertNull(task.getAssignee());

    // Now Dan comes along and claims the approval task
    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(CLAIM, DAN.getUsername()), DAN);
    assertTrue(task.getOwner().equals(DAN.getUsername()));
    assertTrue(task.getAssignee().equals(DAN.getUsername()));

    // Then Dan decides he wants Sue to check the approval, so he delegates to her
    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(DELEGATE, SUE.getUsername()), DAN);
    assertTrue(task.getOwner().equals(DAN.getUsername()));
    assertTrue(task.getAssignee().equals(SUE.getUsername()));
    assertTrue(task.getDelegationState().equals(DelegationState.PENDING));

    // Sue sets the approval result object
    request = requestRepository.findOneByRequestId(request.getRequestId());
    request.setApproval(new Approval(true, ""));
    requestRepository.save(request);

    // Sue resolves the task back to Dan
    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(RESOLVE, null), SUE);
    assertTrue(task.getOwner().equals(DAN.getUsername()));
    assertTrue(task.getAssignee().equals(DAN.getUsername()));
    assertTrue(task.getDelegationState().equals(DelegationState.RESOLVED));

    // Dan completes the task
    taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), DAN);
    task = taskService.getTask(request.getRequestId(), "submit");
    taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), DAN);
  }

  @Test
  public void unclaim() {
    TaskInfo task = taskService.getTask(request.getRequestId(), "edit");
    assertTrue(task.getOwner().equals(BEN.getUsername()));

    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(CLAIM, BEN.getUsername()), BEN);
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    assertTrue(task.getAssignee().equals(BEN.getUsername()));

    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(UNCLAIM, null), BEN);
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    assertNull(task.getAssignee());
  }
}
