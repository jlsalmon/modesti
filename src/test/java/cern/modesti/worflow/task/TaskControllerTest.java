package cern.modesti.worflow.task;

import static cern.modesti.util.TestUtil.getDefaultRequest;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import cern.modesti.Application;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.Request;
import cern.modesti.workflow.task.TaskInfo;


/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource("classpath:modesti-test.properties")
@WebAppConfiguration
public class TaskControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;
  private MockMvc mockMvc;
  private MediaType contentType = new MediaType(MediaTypes.HAL_JSON.getType(), MediaTypes.HAL_JSON.getSubtype());
  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private RuntimeService runtimeService;

  private String processDefinitionId = "create";
  private Request request;
  private String requestId = "1";
  private List<TaskInfo> taskList = new ArrayList<>();

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();

    // How to add tasks to a request? Need to start the process.
    this.request = requestRepository.save(getDefaultRequest());

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    runtimeService.startProcessInstanceByKey(processDefinitionId, requestId, variables);

    taskList.add(new TaskInfo("edit", "Request in preparation", null, new HashSet<>(Arrays.asList("modesti-creators", "modesti-administrators"))));
  }

  @Test
  public void readSingleTask() throws Exception {
    mockMvc.perform(get("/requests/" + requestId + "/tasks/" + this.taskList.get(0).getName()))
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

