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
import java.util.List;

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
   * Create a new entry in the history repository for the given request.
   *
   * @param request the request object
   */
  public void initialiseChangeHistory(Request request) {
    log.info(format("creating new base history record for request #%s", request.getRequestId()));
    RequestHistory entry = new RequestHistory(new ObjectId().toString(), request.getRequestId(), request, new ArrayList<>(), false);
    requestHistoryRepository.save(entry);
  }

  /**
   * Compare the changes from the given, modified request to the original
   * request and save them to the history record.
   *
   * @param modified the modified request
   */
  public void saveChangeHistory(Request modified) {
    log.info(format("processing change history for request #%s", modified.getRequestId()));
    Request original = requestRepository.findOneByRequestId(modified.getRequestId());

    Assert.notNull(modified);
    Assert.notNull(original);
    Assert.isTrue(modified.getRequestId().equals(original.getRequestId()));

    RequestHistory entry = requestHistoryRepository.findOneByRequestId(original.getRequestId());
    if (entry == null) {
      initialiseChangeHistory(original);
      return;
    }

    ChangeEvent event = new ChangeEvent(new DateTime(DateTimeZone.UTC));
    DiffNode root = ObjectDifferBuilder.buildDefault().compare(modified, original);

    root.visit(new PrintingVisitor(modified, original));
    root.visit(new ChangeVisitor(event, modified, original));

    entry.getEvents().add(event);
    requestHistoryRepository.save(entry);
  }

  public List<Change> getChanges(Request modified) {
    RequestHistory entry = requestHistoryRepository.findOneByRequestId(modified.getRequestId());
    Request original = entry.getOriginalRequest();

    ChangeEvent event = new ChangeEvent(new DateTime(DateTimeZone.UTC));
    DiffNode root = ObjectDifferBuilder.buildDefault().compare(modified, original);

    root.visit(new ChangeVisitor(event, modified, original));
    return event.getChanges();
  }
}
