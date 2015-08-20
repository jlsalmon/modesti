package cern.modesti.configuration;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.request.Request;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ConfigurationService {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  RequestRepository requestRepository;

  /**
   * Entry point to the configuration from the workflow engine.
   *
   * @param execution
   * @throws Exception
   */
  public void configureRequest(String requestId, DelegateExecution execution) throws Exception {
    Request request = requestRepository.findOneByRequestId(requestId);
    if (request == null) {
      throw new ActivitiException("No request with id " + requestId + " was found");
    }

    RequestProvider provider = requestProviderRegistry.getPluginFor(request);

    boolean result = provider.configure(request);
    execution.setVariable("configured", result);

    // Store the request
    requestRepository.save(request);
  }
}
