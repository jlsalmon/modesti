package cern.modesti.workflow;

import cern.modesti.request.Request;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * Core functionality for use within workflows.
 * 
 * @author Justin Lewis Salmon
 */
public interface CoreWorkflowService {

  /**
   * Start a new workflow process instance for the given request.
   *
   * @param request the request to be associated with the newly created
   *                workflow process instance
   * @return the newly started process instance object
   */
  ProcessInstance startProcessInstance(Request request);

  /**
   * Retrieve the workflow process instance object associated with a particular
   * request.
   *
   * @param requestId the id of the request
   * @return the {@link ProcessInstance} object associated with the request
   */
  ProcessInstance getProcessInstance(String requestId);
}
