package cern.modesti.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.mongodb.Mongo;

import cern.modesti.repository.request.RequestRepository;
import cern.modesti.repository.request.schema.SchemaRepository;

@Configuration

@EnableMongoRepositories(basePackageClasses = { RequestRepository.class, SchemaRepository.class })
public class MongoConfig //extends AbstractMongoConfiguration {
//
//  @Override
//  protected String getDatabaseName() {
//    return "modestidb";
//  }
//
//  @Override
//  public Mongo mongo() throws Exception {
//    return new Mongo("localhost");
//  }
//  
//  @Bean
//  public MongoRepositoryFactory mongoRepositoryFactory() throws Exception {
//    return new MongoRepositoryFactory(mongoTemplate());
//  }
//  
//  
{
  @Bean
  public ValidatingMongoEventListener validatingMongoEventListener() {
    return new ValidatingMongoEventListener(validator());
  }

  @Bean
  public LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }
}
