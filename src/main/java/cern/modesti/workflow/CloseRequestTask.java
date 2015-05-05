package cern.modesti.workflow;

import cern.modesti.request.Request;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class CloseRequestTask implements JavaDelegate {

  private static final Logger LOG = LoggerFactory.getLogger(CloseRequestTask.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Request request = execution.getVariable("request", Request.class);
    LOG.info("closing request id " + request.getRequestId() + "...");
  }
}
