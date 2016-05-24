package cern.modesti.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * This class listens for create, update and delete events for individual
 * requests.
 * <p>
 * The {@link RequestRepository} is automatically exposed
 * as a REST resource via Spring Data REST, hence why there is no explicit MVC
 * controller for it. This class simply hooks into the Spring Data REST
 * lifecycle and intercepts request create/save events, and lets Spring Data
 * REST do everything else automatically.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
@RepositoryEventHandler(Request.class)
public class RequestRepositoryEventHandler {

  @Autowired
  private RequestService requestService;

  @HandleBeforeCreate
  public void handleRequestCreate(Request request) {
    requestService.insert(request);
  }

  @HandleBeforeSave
  public void handleRequestSave(Request request) {
    requestService.save(request);
  }

  @HandleAfterDelete
  public void handleAfterDelete(Request request) {
    requestService.delete(request);
  }
}
