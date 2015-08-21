/**
 *
 */
package cern.modesti.config.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cern.modesti.request.counter.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;


/**
 * @author Justin Lewis Salmon
 *
 */
@Service
@Profile({"test", "dev"})
public class CounterInitialiser {

  private static final Logger LOG = LoggerFactory.getLogger(CounterInitialiser.class);

  /**
   * This method will initialise the counter collections in a test environment.
   */
  @Autowired
  public CounterInitialiser(MongoOperations mongo) {
    LOG.info("Initialising counters");

    if (!mongo.collectionExists(Counter.class)) {
      mongo.createCollection(Counter.class);
      Counter requestCounter = new Counter("requests", 0L);
      mongo.insert(requestCounter);
    }
  }
}
