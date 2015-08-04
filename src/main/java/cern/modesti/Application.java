package cern.modesti;

import cern.modesti.config.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * TODO
 *
 * To activate a profile, pass -Dspring.profiles.active=[test|dev|prod]
 *
 * Service                      Profiles
 * --------------------------------------------------------------------
 * CounterInitialiser           test
 * DomainInitialiser            test
 * SchemaInitialiser            test
 * ConfigurationService
 * UploadService
 * NotificationService
 * CounterService
 * SchemaService
 * OptionService
 * LdapSynchroniser             dev, prod
 * ValidationService
 * WorkflowService
 * HistoryService
 *
 *
 * Configuration                Profiles
 * --------------------------------------------------------------------
 * CacheConfig
 * DataSourceConfig
 * JpaConfig
 * Mongoconfig
 * RestConfig
 * WebSecurityconfig            dev, prod
 * WorkflowConfig               dev, prod (usersAndGroupsInitializer)
 *
 *
 *
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).properties("spring.config.name:modesti").sources(Application.class).build().run(args);
  }
}
