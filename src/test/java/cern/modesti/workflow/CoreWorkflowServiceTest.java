package cern.modesti.workflow;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.user.User;
import cern.modesti.workflow.task.TaskAction;
import cern.modesti.workflow.task.TaskInfo;
import cern.modesti.workflow.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;

import static cern.modesti.workflow.task.TaskAction.Action.ASSIGN;
import static cern.modesti.workflow.task.TaskAction.Action.COMPLETE;
import static org.junit.Assert.assertEquals;

/**
 * @author Justin Lewis Salmon
 */
public class CoreWorkflowServiceTest  {

  @Autowired
  TaskService taskService;

  @Autowired
  RequestRepository requestRepository;

  /**
   *
   * @param requestId
   * @param taskName
   * @param status
   */
  private void assertTaskNameAndRequestStatus(String requestId, String taskName, String status) {
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
    taskService.execute(requestId, task.getName(), new TaskAction(ASSIGN, user.getUsername()), user);
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
