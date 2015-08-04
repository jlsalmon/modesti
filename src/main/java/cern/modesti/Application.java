package cern.modesti;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * TODO
 *
 * "test" profile should use:
 *  - In-memory JDBC datasource (h2)        - check
 *  - In-memory MongoDB datasource (fongo)
 *  - In-memory LDAP authentication
 *
 * "dev" and "prod" should use real stuff.
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
