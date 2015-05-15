package cern.modesti.workflow.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class RequestConfigurationTask implements JavaDelegate {

  private static final Logger LOG = LoggerFactory.getLogger(RequestConfigurationTask.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String requestId = execution.getVariable("requestId", String.class);
    LOG.info("configuring points for request id " + requestId + "...");
  }
}
