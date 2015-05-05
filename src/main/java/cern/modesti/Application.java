package cern.modesti;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableEntityLinks;

import cern.modesti.config.DataSourceConfig;
import cern.modesti.config.JpaConfig;
import cern.modesti.config.MongoConfig;
import cern.modesti.config.RestConfig;
import cern.modesti.config.WebSecurityConfig;

/**
 * TODO
 *
 * To run with test profile, pass -Dspring.profiles.active=test
 * To run with prod profile, pass -Dspring.profiles.active=prod
 *
 * @author Justin Lewis Salmon
 */
@SpringBootApplication
@EnableEntityLinks
@Import({DataSourceConfig.class, JpaConfig.class, MongoConfig.class, RestConfig.class, WebSecurityConfig.class})
public class Application {

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).properties("spring.config.name:modesti").sources(Application.class).build().run(args);
  }
}
