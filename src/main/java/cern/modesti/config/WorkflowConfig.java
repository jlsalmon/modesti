package cern.modesti.config;

import cern.modesti.workflow.task.UserTaskAssignmentHandler;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for customising the Activiti workflow engine.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class WorkflowConfig {

  @Autowired
  private UserTaskAssignmentHandler userTaskAssignmentHandler;

  /**
   * Customise the Activiti {@link org.activiti.engine.ProcessEngineConfiguration}.
   *
   * @param engineConfiguration
   * @return
   */
  @Bean
  public InitializingBean activitiConfigurer(SpringProcessEngineConfiguration engineConfiguration) {
    return () -> engineConfiguration.getBpmnParser().getBpmnParserHandlers().addHandler(userTaskAssignmentHandler);
  }
}


