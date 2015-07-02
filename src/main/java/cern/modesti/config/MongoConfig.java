package cern.modesti.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

  @Autowired
  private Environment env;

  @Override
  protected String getDatabaseName() {
    return env.getProperty("mongodb.db");
  }

  @Override
  public Mongo mongo() throws Exception {
    return new MongoClient(env.getProperty("mongodb.host"));
  }

  @Bean
  public MongoRepositoryFactory mongoRepositoryFactory() throws Exception {
    return new MongoRepositoryFactory(mongoTemplate());
  }

  @Bean
  public ValidatingMongoEventListener validatingMongoEventListener() {
    return new ValidatingMongoEventListener(validator());
  }

  @Bean
  public LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }
}
