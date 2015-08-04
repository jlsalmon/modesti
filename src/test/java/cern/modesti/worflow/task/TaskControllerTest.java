package cern.modesti.worflow.task;

import cern.modesti.Application;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import cern.modesti.workflow.task.TaskInfo;
import org.activiti.engine.RuntimeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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
  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private RuntimeService runtimeService;

  private String processDefinitionId = "create-tim-points";
  private Request request;
  private String requestId = "1";
  private List<TaskInfo> taskList = new ArrayList<>();

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();

    // How to add tasks to a request? Need to start the process.
    this.request = requestRepository.save(getTestRequest());

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    runtimeService.startProcessInstanceById(processDefinitionId, requestId, variables);

    taskList.add(new TaskInfo("validate", "", null, new HashSet<>(Collections.singleton("modesti-creators"))));
  }

  @Test
  public void readSingleTask() throws Exception {
    mockMvc.perform(get("/requests/" + requestId + "/tasks/"
        + this.taskList.get(0).getName()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.id", is(this.taskList.get(0).getName())))
        .andExpect(jsonPath("$.uri", is("http://bookmark.com/1/" + requestId)))
        .andExpect(jsonPath("$.description", is("A description")));
  }

  private Request getTestRequest() {
    Request request = new Request();
    request.setType(RequestType.CREATE);
    request.setDescription("description");
    request.setDomain("TIM");
    request.setCategories(new ArrayList<>(Arrays.asList("PLC")));
    return request;
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

