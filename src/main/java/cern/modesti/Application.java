package cern.modesti;

import cern.modesti.plugin.RequestProvider;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.plugin.core.config.EnablePluginRegistries;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * The main entry point for the MODESTI application server.
 *
 * @author Justin Lewis Salmon
 */
@SpringBootApplication(exclude = {EmbeddedMongoAutoConfiguration.class})
@EnablePluginRegistries(RequestProvider.class)
public class Application {

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).properties("spring.config.name:modesti").sources(Application.class).build().run(args);
  }
}
