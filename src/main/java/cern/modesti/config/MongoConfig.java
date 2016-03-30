package cern.modesti.config;

import cern.modesti.util.DateToTimestampConverter;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.NullProcessor;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
@EnableMongoAuditing
@Slf4j
public class MongoConfig extends AbstractMongoConfiguration {

  @Autowired
  private Environment env;

  @Override
  protected String getDatabaseName() {
    return env.getRequiredProperty("mongodb.db");
  }

  @Override
  public Mongo mongo() throws Exception {
    String host = env.getProperty("mongodb.host");

    // If no hostname is supplied in the dev profile, an embedded mongodb will be used
    if (host == null && env.acceptsProfiles("dev")) {
      return embeddedMongo();
    }

    List<ServerAddress> hosts = new ArrayList<>();
    for (Object hostname : env.getRequiredProperty("mongodb.host", List.class)) {
      hosts.add(new ServerAddress((String) hostname));
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

  private Mongo embeddedMongo() throws IOException {
    int port = 12345;
    MongodConfig mongodConfig = new MongodConfig(Version.Main.V2_0, port, Network.localhostIsIPv6(), "/tmp/mongodb-embedded");
    RuntimeConfig runtimeConfig = new RuntimeConfig();
    runtimeConfig.setProcessOutput(new ProcessOutput(new NullProcessor(), new NullProcessor(), new NullProcessor()));
    MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
    MongodExecutable mongodExecutable = runtime.prepare(mongodConfig);
    mongodExecutable.start();
    return new MongoClient("localhost", port);
  }
}
