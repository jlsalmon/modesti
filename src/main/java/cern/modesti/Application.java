package cern.modesti;

import cern.modesti.config.*;
import org.activiti.spring.boot.RestApiAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableEntityLinks;

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
@Import({DataSourceConfig.class, JpaConfig.class, MongoConfig.class, RestConfig.class, WorkflowConfig.class, WebSecurityConfig.class, RestApiAutoConfiguration.class})
public class Application {

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).properties("spring.config.name:modesti").sources(Application.class).build().run(args);
  }
}
