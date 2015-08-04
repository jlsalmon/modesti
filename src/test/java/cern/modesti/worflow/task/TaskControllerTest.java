package cern.modesti.worflow.task;

import cern.modesti.Application;
import cern.modesti.repository.jpa.subsystem.SubSystem;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import cern.modesti.security.ldap.Role;
import cern.modesti.security.ldap.User;
import cern.modesti.workflow.task.TaskInfo;
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

import java.io.IOException;
import java.util.*;

import static org.hamcrest.core.Is.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
  private MediaType contentType = new MediaType(MediaTypes.HAL_JSON.getType(), MediaTypes.HAL_JSON.getSubtype());
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
    runtimeService.startProcessInstanceByKey(processDefinitionId, requestId, variables);

    taskList.add(new TaskInfo("validate", "Validate task", null, new HashSet<>(Arrays.asList("modesti-creators", "modesti-administrators"))));
  }

  @Test
  public void readSingleTask() throws Exception {
    mockMvc.perform(get("/requests/" + requestId + "/tasks/" + this.taskList.get(0).getName()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.name", is(this.taskList.get(0).getName())))
        .andExpect(jsonPath("$.description", is(this.taskList.get(0).getDescription())))
        .andExpect(jsonPath("$.assignee", is(this.taskList.get(0).getAssignee())));
        //.andExpect(jsonPath("$.candidateGroups", contains(this.taskList.get(0).getCandidateGroups())));
  }

  private Request getTestRequest() {
    Request request = new Request();
    request.setRequestId("1");
    request.setType(RequestType.CREATE);
    request.setCreator(new User(1, "bert", "Bert", "Is Evil", "bert@modesti.ch", new HashSet<>(Collections.singleton(new Role("modesti-administrators")))));
    request.setDescription("description");
    request.setDomain("TIM");
    request.setSubsystem(new SubSystem(1L, "EAU DEMI", "EAU", "A", "DEMI", "B"));
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

