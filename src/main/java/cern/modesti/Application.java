package cern.modesti;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.point.PointRepository;
import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
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
 * ConfigurationService
 * UploadService
 * NotificationService
 * CounterService
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
    ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class).properties("spring.config.name:modesti").sources(Application.class).build().run(args);

    RequestRepository requestRepository = context.getBean(RequestRepository.class);
    PointRepository pointRepository = context.getBean(PointRepository.class);

    Request request = requestRepository.findOneByRequestId("646");
      pointRepository.save(request.getPoints());


    System.out.println("loaded points");
  }
}
