package cern.modesti.config;


import org.activiti.engine.test.mock.MockExpressionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import cern.modesti.workflow.WorkflowService;


/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Profile({"test", "dev", "prod"})
public class WorkflowConfig {

  @Bean
  public WorkflowService workflowService() {
    return new WorkflowService();
  }
}

