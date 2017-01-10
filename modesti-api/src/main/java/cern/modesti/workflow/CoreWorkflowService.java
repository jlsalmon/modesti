package cern.modesti.workflow;

import cern.modesti.request.Request;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Justin Lewis Salmon
 */
public interface CoreWorkflowService {

  ProcessInstance startProcessInstance(Request request);

  ProcessInstance getProcessInstance(String requestId);
}
