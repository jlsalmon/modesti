package cern.modesti.config;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import cern.modesti.util.DateToTimestampConverter;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for the core {@link Mongo} client and related MongoDB
 * beans.
 *
 * @author Justin Lewis Salmon
 */
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
  public MongoClient mongoClient() {
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
  public MongoCustomConversions customConversions() {
    return new MongoCustomConversions(singletonList(new DateToTimestampConverter()));
  }

  private MongoClient embeddedMongo() {
    int port = 12345;
    try {
      MongodConfigBuilder builder = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
          .net(new Net(port, Network.localhostIsIPv6()));

      if (env.containsProperty("mongodb.persistent")
          && env.getProperty("mongodb.persistent", Boolean.class).equals(true)) {
        builder.replication(new Storage("/tmp/mongodb-embedded", null, 0));
      }

      IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaultsWithLogger(Command.MongoD, log)
          .processOutput(ProcessOutput.getDefaultInstanceSilent()).build();

      MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
      MongodExecutable mongodExecutable = runtime.prepare(builder.build());
      mongodExecutable.start();
      return new MongoClient("localhost", port);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
