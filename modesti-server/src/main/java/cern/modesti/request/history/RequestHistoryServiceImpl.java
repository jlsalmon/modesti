package cern.modesti.request.history;

import cern.modesti.request.Request;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaImpl;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.schema.UnknownSchemaException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

  /**
   * Finds one request by request id. The method is a workaround to fix the problem
   * when there are more than one entries in the repository with the same requestID
   * @param id the request id
   * @return The request matching the requestId or null if it was not found.
   */
  public RequestHistory findOneByRequestId(String id) {
    List<? extends RequestHistory> entries = requestHistoryRepository.findAllByRequestId(id);

    if (entries != null && !entries.isEmpty()) {
      // Bug fix... In case there are more than 1 entries, find the one 
      // with more events and chose that one...
      RequestHistory entry = entries.get(0);
      for (RequestHistory r : entries) {
        if (entry==null || r.getEvents().size() > entry.getEvents().size()) {
          entry = r;
        }
      }
      return entry;
    }
    
    return null;
  }
  
  @Override
  public void initialiseChangeHistory(Request request) {
    log.info(format("creating new base history record for request #%s", request.getRequestId()));

    RequestHistory entry = findOneByRequestId(request.getRequestId());
    if (entry != null) {
      return;
    }
    
    Optional<SchemaImpl> schemaOpt = schemaRepository.findById(request.getDomain());
    if (!schemaOpt.isPresent()) {
      throw new UnknownSchemaException(request.getDomain());
    }
    
    Schema schema = schemaOpt.get();
    entry = new RequestHistoryImpl(new ObjectId().toString(), request.getRequestId(),
        schema.getIdProperty(), request, new ArrayList<>(), false);
    requestHistoryRepository.save((RequestHistoryImpl) entry);
  }


  @Override
  public void saveChangeHistory(Request modified) {
    log.info(format("processing change history for request #%s", modified.getRequestId()));
    RequestHistoryImpl entry = (RequestHistoryImpl) findOneByRequestId(modified.getRequestId());
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
    RequestHistory entry = findOneByRequestId(request.getRequestId());
    if (entry != null && !entry.getEvents().isEmpty()) {
      return entry.getEvents().get(0).getChanges();
    } else {
      return new ArrayList<>();
    }
  }


  @Override
  public void deleteChangeHistory(Request request) {
    log.info(format("deleting change history for request #%s", request.getRequestId()));
    RequestHistory entry = findOneByRequestId(request.getRequestId());
    if (entry != null) {
      requestHistoryRepository.deleteById(entry.getId());
    }
  }
}
