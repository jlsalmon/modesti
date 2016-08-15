package cern.modesti.workflow.task;

import cern.modesti.request.Request;
import cern.modesti.test.BaseIntegrationTest;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import static cern.modesti.test.TestUtil.getDummyRequest;

/**
 * @author Justin Lewis Salmon
 */
@Ignore
public class TaskServiceTest extends BaseIntegrationTest {

  @Autowired
  TaskService taskService;

  Request request;
  ProcessInstance process;

  @Before
  public void setUp() {
    request = getDummyRequest();
    process = coreWorkflowService.startProcessInstance(request);
  }

  @After
  public void tearDown() {
    runtimeService.deleteProcessInstance(process.getProcessInstanceId(), null);
    requestService.delete(request);
  }

//  @Test
//  public void delegate() {
//    TaskInfo task = taskService.getTask(request.getRequestId(), "task1");
//    // Ben claims the task
//    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(CLAIM, BEN.getUsername()), BEN);
//    assertTrue(task.getAssignee().equals(BEN.getUsername()));
//
//    // Ben delegates the task to Joe
//    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(DELEGATE, JOE.getUsername()), BEN);
//    assertTrue(task.getOwner().equals(BEN.getUsername()));
//    assertTrue(task.getAssignee().equals(JOE.getUsername()));
//    assertTrue(task.getDelegationState().equals(DelegationState.PENDING));
//
//    try {
//      // Joe should NOT be able to complete the task, only resolve it back to Ben
//      taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), JOE);
//      fail("Delegates should not be able to complete tasks");
//    } catch (ActivitiException ignored) {
//    }
//
//    // Joe resolves the task back to Ben
//    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(RESOLVE, null), JOE);
//    assertTrue(task.getOwner().equals(BEN.getUsername()));
//    assertTrue(task.getAssignee().equals(BEN.getUsername()));
//    assertTrue(task.getDelegationState().equals(DelegationState.RESOLVED));
//
//    // Ben may now complete the task
//    taskService.execute(request.getRequestId(), task.getName(), new TaskAction(COMPLETE, null), BEN);
//    task = taskService.getTask(request.getRequestId(), "task2");
//    assertNull(task.getAssignee());
//  }

//  @Test
//  public void unclaim() {
//    TaskInfo task = taskService.getTask(request.getRequestId(), "task1");
//    //assertTrue(task.getOwner().equals(BEN.getUsername()));
//
//    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(CLAIM, BEN.getUsername()), BEN);
//    //assertTrue(task.getOwner().equals(BEN.getUsername()));
//    assertTrue(task.getAssignee().equals(BEN.getUsername()));
//
//    task = taskService.execute(request.getRequestId(), task.getName(), new TaskAction(UNCLAIM, null), BEN);
//    //assertTrue(task.getOwner().equals(BEN.getUsername()));
//    assertNull(task.getAssignee());
//  }
}
