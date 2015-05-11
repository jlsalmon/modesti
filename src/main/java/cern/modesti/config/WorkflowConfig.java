package cern.modesti.config;

import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import cern.modesti.security.ldap.LdapGroupManager;
import cern.modesti.security.ldap.LdapGroupManagerFactory;
import cern.modesti.security.ldap.LdapUserManager;
import cern.modesti.security.ldap.LdapUserManagerFactory;
import cern.modesti.workflow.RequestStatusManager;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Profile("test")
public class WorkflowConfig {

  @Autowired
  Environment env;

  @Bean
  public RequestStatusManager requestStatusManager() {
    return new RequestStatusManager();
  }

  @Bean
  InitializingBean ldapConfigurationInitializer(final SpringProcessEngineConfiguration configuration) {

    return new InitializingBean() {
      @Override
      public void afterPropertiesSet() {
        configuration.getSessionFactories().put(UserIdentityManager.class, userManagerFactory());
        configuration.getSessionFactories().put(GroupIdentityManager.class, groupManagerFactory());
      }
    };
  }

  @Bean
  LdapUserManagerFactory userManagerFactory() {
    return new LdapUserManagerFactory(userManager());
  }

  @Bean
  LdapGroupManagerFactory groupManagerFactory() {
    return new LdapGroupManagerFactory(groupManager());
  }

  @Bean
  LdapUserManager userManager() {
    return new LdapUserManager(anonymousLdapTemplate());
  }

  @Bean
  LdapGroupManager groupManager() {
    return new LdapGroupManager(anonymousLdapTemplate());
  }

  @Bean
  public LdapTemplate anonymousLdapTemplate() {
    return new SpringSecurityLdapTemplate(anonymousContextSource());
  }

  @Bean
  public LdapContextSource anonymousContextSource() {
    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(env.getRequiredProperty("ldap.anon.url"));
    contextSource.setBase(env.getRequiredProperty("ldap.base"));
    contextSource.setAnonymousReadOnly(true);
    return contextSource;
  }
}