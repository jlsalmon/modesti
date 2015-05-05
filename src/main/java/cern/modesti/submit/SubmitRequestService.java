/**
 *
 */
package cern.modesti.submit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;

/**
 * @author Justin Lewis Salmon
 *
 */
@Service
@Transactional
public class SubmitRequestService {

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private TaskService taskService;

  public void submitRequest(final String requestId) {
    Request request = requestRepository.findOneByRequestId(requestId);

    Map<String, Object> variables = new HashMap<>();
    variables.put("request", request);

    runtimeService.startProcessInstanceByKey("createTimPoints", variables);
  }

  public List<Task> getTasks() {
    return taskService.createTaskQuery().taskUnassigned().list();
  }
}
