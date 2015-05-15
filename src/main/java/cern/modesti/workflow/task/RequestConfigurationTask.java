package cern.modesti.workflow.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Random;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Transactional
public class RequestConfigurationTask {

  private static final Logger LOG = LoggerFactory.getLogger(RequestConfigurationTask.class);


  public void configurePoints(String requestId, DelegateExecution execution) {
    LOG.info("configuring points for request id " + requestId + "...");


    /**
     * TODO: implement actual point configuration here
     */


    // Randomly fail the configuration
    boolean failed = new Random(System.currentTimeMillis()).nextBoolean();

    execution.setVariable("configurationFailure", failed);
  }
}
