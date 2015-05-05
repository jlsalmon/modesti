package cern.modesti.config;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.ldap.LDAPConfigurator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Profile("test")
public class WorkflowConfig {

//  @Bean
//  InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {
//
//    return new InitializingBean() {
//      public void afterPropertiesSet() throws Exception {
//
//        Group group = identityService.newGroup("user");
//        group.setName("users");
//        group.setType("security-role");
//        identityService.saveGroup(group);
//
//        User admin = identityService.newUser("admin");
//        admin.setPassword("admin");
//        identityService.saveUser(admin);
//
//      }
//    };
//  }

  @Bean
  LDAPConfigurator ldapConfigurator() {

    System.out.println("creating ldap bean >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    LDAPConfigurator ldapConfigurator = new LDAPConfigurator();
    ldapConfigurator.setUser("abc");
    return ldapConfigurator;
  }
}
