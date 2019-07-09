package cern.modesti;

import cern.modesti.plugin.RequestProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.plugin.core.config.EnablePluginRegistries;

/**
 * The main entry point for the MODESTI application server.
 *
 * @author Justin Lewis Salmon
 */
@SpringBootApplication(exclude = {EmbeddedMongoAutoConfiguration.class})
@EnablePluginRegistries(RequestProvider.class)
public class ModestiServer {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ModestiServer.class).properties("spring.config.name:modesti").sources(ModestiServer.class).build().run(args);
  }
}
