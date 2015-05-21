package cern.modesti.config;


import cern.modesti.security.ldap.LdapSynchroniser;
import cern.modesti.workflow.WorkflowService;
import cern.modesti.workflow.listener.RequestStatusManager;
import cern.modesti.workflow.task.RequestConfigurationTask;
import cern.modesti.workflow.task.RequestSplittingTask;
import cern.modesti.workflow.task.RequestValidationTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Configuration
public class WorkflowConfig {

  @Autowired
  Environment env;

  @Bean
  public WorkflowService workflowService() {
    return new WorkflowService();
  }

//  @Bean
//  public RequestStatusManager requestStatusManager() {
//    return new RequestStatusManager();
//  }
//
//  @Bean
//  public RequestValidationTask requestValidationTask() {
//    return new RequestValidationTask();
//  }
//
//  @Bean
//  public RequestConfigurationTask requestConfigurationTask() {
//    return new RequestConfigurationTask();
//  }
//
//  @Bean
//  public RequestSplittingTask requestSplittingTask() {
//    return new RequestSplittingTask();
//  }

  /**
   * Synchronise LDAP users and groups at startup via an InitializingBean.
   *
   * @param ldapSynchroniser the newly created LdapSynchroniser instance
   * @return the initialising bean
   */
  @Bean
  InitializingBean usersAndGroupsInitializer(final LdapSynchroniser ldapSynchroniser) {
    return ldapSynchroniser::synchroniseUsersAndGroups;
  }
}

