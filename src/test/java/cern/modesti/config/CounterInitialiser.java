/**
 *
 */
package cern.modesti.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import cern.modesti.repository.mongo.request.counter.Counter;

/**
 * @author Justin Lewis Salmon
 *
 */
@Service
@Profile("test")
public class CounterInitialiser {

  private static final Logger LOG = LoggerFactory.getLogger(DomainInitialiser.class);

  /**
   * This method will initialise the counter collections in a test environment.
   */
  @Autowired
  public CounterInitialiser(MongoOperations mongo) {
    LOG.info("Initialising counters");

    if (!mongo.collectionExists(Counter.class)) {
      mongo.createCollection(Counter.class);

      Counter requestCounter = new Counter("requests", 0L);
      Counter pointCounter = new Counter("points", 0L);

      List<Counter> counters = new ArrayList<>(Arrays.asList(requestCounter, pointCounter));
      mongo.insert(counters, Counter.class);
    }
  }
}
