package cern.modesti.worflow.task;

import cern.modesti.request.Request;
import cern.modesti.util.BaseIntegrationTest;
import cern.modesti.workflow.task.TaskAction;
import cern.modesti.workflow.task.TaskInfo;
import cern.modesti.workflow.task.TaskService;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
    request = getDefaultRequest();
    process = workflowService.startProcessInstance(request);
  }

  @After
  public void tearDown() {
    requestRepository.delete(request.getRequestId());
    runtimeService.deleteProcessInstance(process.getProcessInstanceId(), null);
  }

  @Test
  public void delegate() {
    TaskInfo task = taskService.getTask(request.getRequestId(), "edit");
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    //assertTrue(task.getAssignee().equals(BEN.getUsername()));

    // Delegate the task to Joe
    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(DELEGATE, JOE.getUsername()), BEN);

    assertTrue(task.getAssignee().equals(JOE.getUsername()));
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    assertTrue(task.getDelegationState().equals(DelegationState.PENDING));

    try {
      // Joe should NOT be able to complete the task, only resolve it back to Ben
      taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), JOE);
      fail("Delegates should not be able to complete tasks");
    } catch (ActivitiException ignored) {
    }

    // Joe should be able to resolve the task back to Ben
    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(RESOLVE, null), JOE);
    assertTrue(task.getOwner().equals(BEN.getUsername()));
    assertTrue(task.getAssignee().equals(BEN.getUsername()));
    assertTrue(task.getDelegationState().equals(DelegationState.RESOLVED));
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
