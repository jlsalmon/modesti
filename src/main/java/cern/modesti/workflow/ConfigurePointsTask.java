package cern.modesti.workflow;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class ConfigurePointsTask implements JavaDelegate {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurePointsTask.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String requestId = execution.getVariable("requestId", String.class);
    LOG.info("configuring points for request id " + requestId + "...");
  }
}
