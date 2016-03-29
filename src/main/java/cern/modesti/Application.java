package cern.modesti;

import cern.modesti.plugin.RequestProvider;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.plugin.core.config.EnablePluginRegistries;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * To activate a profile, pass -Dspring.profiles.active=[dev|test|prod]
 *
 * The "dev" profile uses:
 *  - In-memory JDBC datasource (h2)
 *  - In-memory MongoDB datasource (fongo)
 *  - No authentication
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
