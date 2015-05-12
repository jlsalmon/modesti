package cern.modesti.workflow.listener;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Transactional
public class RequestStatusManager {
  private static final Logger LOG = LoggerFactory.getLogger(RequestStatusManager.class);

  @Autowired
  private RequestRepository requestRepository;

  /**
   * @param requestId
   * @param status
   */
  public void setRequestStatus(String requestId, String status) {
    LOG.info("setting status " + status + " on request id " + requestId);

    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    request.setStatus(Request.RequestStatus.valueOf(status));
    requestRepository.save(request);
  }
}
