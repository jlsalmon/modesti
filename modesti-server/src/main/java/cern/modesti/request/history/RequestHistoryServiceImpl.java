package cern.modesti.request.history;

import cern.modesti.request.Request;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
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
@Service("requestHistoryService")
@Slf4j
public class RequestHistoryServiceImpl implements RequestHistoryService {

  @Autowired
  private RequestHistoryRepository requestHistoryRepository;

  @Autowired
  private SchemaRepository schemaRepository;

  
  @Override
  public void initialiseChangeHistory(Request request) {
    log.info(format("creating new base history record for request #%s", request.getRequestId()));

    Schema schema = schemaRepository.findOne(request.getDomain());
    RequestHistoryImpl entry = new RequestHistoryImpl(new ObjectId().toString(), request.getRequestId(),
        schema.getIdProperty(), request, new ArrayList<>(), false);
    requestHistoryRepository.save(entry);
  }


  @Override
  public void saveChangeHistory(Request modified) {
    log.info(format("processing change history for request #%s", modified.getRequestId()));
    RequestHistoryImpl entry = requestHistoryRepository.findOneByRequestId(modified.getRequestId());
    if (entry == null) {
      initialiseChangeHistory(modified);
      return;
    }

    Request original = entry.getOriginalRequest();

    Assert.notNull(modified);
    Assert.notNull(original);
    Assert.isTrue(modified.getRequestId().equals(original.getRequestId()));

    entry.setEvents(Collections.singletonList(RequestDiffer.diff(modified, original, entry.getIdProperty())));
    requestHistoryRepository.save(entry);
  }

  @Override
  public List<Change> getChanges(Request request) {
    RequestHistory entry = requestHistoryRepository.findOneByRequestId(request.getRequestId());
    if (entry != null && !entry.getEvents().isEmpty()) {
      return entry.getEvents().get(0).getChanges();
    } else {
      return new ArrayList<>();
    }
  }


  @Override
  public void deleteChangeHistory(Request request) {
    log.info(format("deleting change history for request #%s", request.getRequestId()));
    RequestHistory entry = requestHistoryRepository.findOneByRequestId(request.getRequestId());
    requestHistoryRepository.delete(entry.getId());
  }
}
