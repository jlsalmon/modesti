package cern.modesti.request.counter;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class CounterServiceImpl implements CounterService {

  @Autowired
  private MongoOperations mongo;

  @PostConstruct
  public void init() {
    log.info("Initialising counters");

    if (!mongo.collectionExists(Counter.class)) {
      mongo.createCollection(Counter.class);
      Counter requestCounter = new Counter("requests", 0L);
      mongo.insert(requestCounter);
    }
  }

  @Override
  public Long getNextSequence(String collectionName) {
    Counter counter = mongo.findAndModify(query(where("_id").is(collectionName)), new Update().inc("sequence", 1), options().returnNew(true), Counter.class);
    return counter.getSequence();
  }
}
