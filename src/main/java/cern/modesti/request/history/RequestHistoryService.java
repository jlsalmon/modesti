package cern.modesti.request.history;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.comparison.ComparisonStrategy;
import de.danielbechler.diff.comparison.PrimitiveDefaultValueMode;
import de.danielbechler.diff.node.DiffNode;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;

import static java.lang.String.format;

/**
 * This service class is responsible for processing and storing changes made to
 * a request object. Each {@link Request} has a corresponding
 * {@link RequestHistory} entry, which records changes made to the request as
 * successive diffs.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class RequestHistoryService {

  @Autowired
  private RequestHistoryRepository requestHistoryRepository;

  @Autowired
  private RequestRepository requestRepository;

  /**
   *
   * @param request
   */
  public void initialiseChangeHistory(Request request) {
    log.info(format("creating new base history record for request #%s", request.getRequestId()));
    RequestHistory entry = new RequestHistory(new ObjectId().toString(), request.getRequestId(), request, new ArrayList<>(), false);
    requestHistoryRepository.save(entry);
  }

  /**
   *
   * @param modified
   */
  public void saveChangeHistory(Request modified) {
    log.info(format("processing change history for request #%s", modified.getRequestId()));
    Request original = requestRepository.findOneByRequestId(modified.getRequestId());

    Assert.notNull(modified);
    Assert.notNull(original);
    Assert.isTrue(modified.getRequestId().equals(original.getRequestId()));

    // TODO: fix this. If the type of a point property changes, the differ craps out. See https://github.com/SQiShER/java-object-diff/issues/56

    RequestHistory entry = requestHistoryRepository.findOneByRequestId(original.getRequestId());
    if (entry == null) {
      initialiseChangeHistory(original);
      return;
    }

    ChangeEvent event = new ChangeEvent(new DateTime(DateTimeZone.UTC));
    DiffNode root = ObjectDifferBuilder.startBuilding().comparison().ofType(String.class).toUse((node, type, working, base) -> {
      log.trace("comparing");
    }).and().build().compare(modified, original);

    root.visit(new PrintingVisitor(modified, original));
    root.visit(new ChangeVisitor(event, modified, original));

    entry.getEvents().add(event);
    requestHistoryRepository.save(entry);
  }
}
