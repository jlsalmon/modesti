package cern.modesti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import cern.modesti.config.DataSourceConfig;
import cern.modesti.config.JpaConfig;
import cern.modesti.config.MongoConfig;
import cern.modesti.config.RestConfig;

@SpringBootApplication
@Import( {DataSourceConfig.class, JpaConfig.class, MongoConfig.class, RestConfig.class} )
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
