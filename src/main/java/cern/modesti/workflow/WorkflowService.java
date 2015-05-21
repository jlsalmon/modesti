/**
 *
 */
package cern.modesti.workflow;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import org.activiti.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 *
 */
@Service
@Transactional
public class WorkflowService {
  private static final Logger LOG = LoggerFactory.getLogger(WorkflowService.class);

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private RuntimeService runtimeService;

  /**
   *
   * @param request
   */
  public void startProcessInstance(final Request request) {
    LOG.info("starting process for request " + request.getRequestId());
    request.setStatus(Request.RequestStatus.IN_PROGRESS);

    Map<String, Object> variables = new HashMap<>();
    variables.put("requestId", request.getRequestId());
    variables.put("requiresApproval", request.requiresApproval());
    variables.put("requiresCabling", request.requiresCabling());

    runtimeService.startProcessInstanceByKey("create-tim-points", request.getRequestId(), variables);
  }
}
