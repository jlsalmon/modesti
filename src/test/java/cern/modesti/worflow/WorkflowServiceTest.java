package cern.modesti.worflow;

import cern.modesti.Application;
import cern.modesti.request.Request;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

import java.util.HashMap;
import java.util.Map;

import static cern.modesti.util.TestUtil.getTestRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource("classpath:modesti-test.properties")
@WebIntegrationTest
public class WorkflowServiceTest {

  @Autowired
  RuntimeService runtimeService;

  @Autowired
  TaskService taskService;

  @Autowired
  HistoryService historyService;

  Wiser wiser;

  @Before
  public void setUp() throws Exception {
    wiser = new Wiser();
    wiser.start();
  }

  @After
  public void tearDown() throws Exception {
    wiser.stop();
  }

  @Test
  public void testHappyPath() {

    Request request = getTestRequest();

    // Start process instance
    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("create-tim-points-0.2", variables);

    // First, the 'edit' task should be active
    Task task = taskService.createTaskQuery().processInstanceBusinessKey(request.getRequestId()).active().singleResult();
    Assert.assertEquals("edit", task.getName());

//    // Completing the phone interview with success should trigger two new tasks
//    Map<String, Object> taskVariables = new HashMap<String, Object>();
//    taskVariables.put("telephoneInterviewOutcome", true);
//    taskService.complete(task.getId(), taskVariables);
//
//    List<Task> tasks = taskService.createTaskQuery()
//        .processInstanceId(processInstance.getId())
//        .orderByTaskName().asc()
//        .list();
//    Assert.assertEquals(2, tasks.size());
//    Assert.assertEquals("Financial negotiation", tasks.get(0).getName());
//    Assert.assertEquals("Tech interview", tasks.get(1).getName());
//
//    // Completing both should wrap up the subprocess, send out the 'welcome mail' and end the process instance
//    taskVariables = new HashMap<String, Object>();
//    taskVariables.put("techOk", true);
//    taskService.complete(tasks.get(0).getId(), taskVariables);
//
//    taskVariables = new HashMap<String, Object>();
//    taskVariables.put("financialOk", true);
//    taskService.complete(tasks.get(1).getId(), taskVariables);
//
//    // Verify email
//    Assert.assertEquals(1, wiser.getMessages().size());
//
//    // Verify process completed
//    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());

  }
}