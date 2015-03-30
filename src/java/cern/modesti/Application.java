package cern.modesti;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import cern.modesti.Application.CustomRepositoryRestMvcConfiguration;

@ComponentScan
@Configuration
//@EnableMongoRepositories/(repositoryFactoryBeanClass=PersonRepositoryFactoryBean.class)
@Import(CustomRepositoryRestMvcConfiguration.class)
@EnableAutoConfiguration
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public ValidatingMongoEventListener validatingMongoEventListener() {
    return new ValidatingMongoEventListener(validator());
  }

  @Bean
  public LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }

  /**
   * Allows validation errors to be converted to REST responses
   *
   * @author Justin Lewis Salmon
   */
  @Configuration
  protected static class CustomRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {

    @Autowired
    private Validator validator;

    @Override
    protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
      validatingListener.addValidator("beforeCreate", validator);
      validatingListener.addValidator("beforeSave", validator);
    }

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
      super.configureRepositoryRestConfiguration(config);
      config.exposeIdsFor(Request.class, Point.class);
      System.out.println(">>>>>>>>>>>>> " + config.isIdExposedFor(Request.class) + " " + config.isIdExposedFor(Point.class));
    }
  }
}
