package cern.modesti;

import cern.modesti.plugin.RequestProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.plugin.core.config.EnablePluginRegistries;

/**
 * To activate a profile, pass -Dspring.profiles.active=[test|dev|prod]
 *
 * The "test" profile uses:
 *  - In-memory JDBC datasource (h2)
 *  - In-memory MongoDB datasource (fongo)
 *  - In-memory LDAP authentication
 *
 * The "dev" profile uses timdb-test, and the "prod" profile uses timdb-pro.
 *
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
@EnablePluginRegistries(RequestProvider.class)
public class Application {

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).properties("spring.config.name:modesti").sources(Application.class).build().run(args);
  }
}
