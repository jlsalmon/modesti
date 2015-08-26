package cern.modesti.worflow.task;

import cern.modesti.request.Request;
import cern.modesti.util.BaseIntegrationTest;
import cern.modesti.workflow.task.TaskInfo;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static cern.modesti.util.TestUtil.getDefaultRequest;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class TaskControllerTest extends BaseIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;
  private MediaType contentType = new MediaType(MediaTypes.HAL_JSON.getType(), MediaTypes.HAL_JSON.getSubtype());
  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  private Request request;
  private ProcessInstance process;
  private List<TaskInfo> taskList = new ArrayList<>();

  @Before
  public void setup() throws Exception {
    mockMvc = webAppContextSetup(webApplicationContext).build();
    request = getDefaultRequest();
    process = workflowService.startProcessInstance(request);

    taskList.add(new TaskInfo("edit", "Request in preparation", null, null, null, new HashSet<>(Arrays.asList("modesti-creators", "modesti-administrators"))));
  }

  @After
  public void tearDown() {
    runtimeService.deleteProcessInstance(process.getProcessInstanceId(), null);
  }

  @Test
  public void readSingleTask() throws Exception {
    mockMvc.perform(get("/requests/" + request.getRequestId() + "/tasks/" + this.taskList.get(0).getName()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.name", is(this.taskList.get(0).getName())))
        .andExpect(jsonPath("$.description", is(this.taskList.get(0).getDescription())))
        .andExpect(jsonPath("$.assignee", is(this.taskList.get(0).getAssignee())))
        .andExpect(jsonPath("$.candidateGroups", contains(this.taskList.get(0).getCandidateGroups().toArray())));
  }



  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {
    this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny()
        .get();
    Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  protected String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
  }
}

