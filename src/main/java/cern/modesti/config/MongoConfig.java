package cern.modesti.config;

import cern.modesti.util.DateToTimestampConverter;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
@EnableMongoAuditing
@Profile({"test", "prod"})
public class MongoConfig extends AbstractMongoConfiguration {

  @Autowired
  private Environment env;

  @Override
  protected String getDatabaseName() {
    return env.getRequiredProperty("mongodb.db");
  }

  @Override
  public Mongo mongo() throws Exception {
    List<ServerAddress> hosts = new ArrayList<>();
    for (Object host : env.getRequiredProperty("mongodb.host", List.class)) {
      hosts.add(new ServerAddress((String) host));
    }

    String username = env.getRequiredProperty("mongodb.username");
    String password = env.getRequiredProperty("mongodb.password");
    MongoCredential credential = MongoCredential.createCredential(username, getDatabaseName(), password.toCharArray());

    return new MongoClient(hosts, singletonList(credential));
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

  @Override
  public CustomConversions customConversions() {
    return new CustomConversions(singletonList(new DateToTimestampConverter()));
  }
}
