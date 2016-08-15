package cern.modesti.workflow.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * REST controller for retrieving workflow history for
 * {@link cern.modesti.request.Request} instances.
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class HistoryController {

  @Autowired
  HistoryService historyService;

  @RequestMapping(value = "/api/requests/{id}/history", method = GET)
  public HttpEntity<Resources<HistoricEvent>> getHistory(@PathVariable("id") String id) {
    Resources<HistoricEvent> history = new Resources<>(historyService.getHistoryForRequest(id));
    return new ResponseEntity<>(history, HttpStatus.OK);
  }
}
