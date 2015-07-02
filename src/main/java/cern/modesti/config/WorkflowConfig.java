package cern.modesti.config;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import cern.modesti.security.ldap.LdapSynchroniser;
import cern.modesti.workflow.WorkflowService;


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

