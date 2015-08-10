package cern.modesti.request.metrics;

import cern.modesti.request.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * Methods for:
 *
 * - Number of requests of a particular status
 * -
 *
 * - Distinct request creators
 *
 * @author Justin Lewis Salmon
 */
@RestController
@Slf4j
public class MetricsController {

  @Autowired
  private MongoTemplate mongoTemplate;

  @RequestMapping(value = "/metrics", method = GET)
  public List getRequestMetrics() {

    GroupByResults<Request> results = mongoTemplate.group("request", GroupBy.key("status").initialDocument("{ count: 0 }").reduceFunction("function" + "" +
        "(doc, prev) { prev.count += 1 }"), Request.class);

    List l = (List) results.getRawResults().get("retval");
    return l;
  }
}
