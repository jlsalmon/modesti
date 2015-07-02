package cern.modesti.workflow.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class HistoryController {

  @Autowired
  HistoryService historyService;

  /**
   * TODO
   *
   * @param id
   * @return
   */
  @RequestMapping(value = "/requests/{id}/history", method = GET)
  public HttpEntity<Resources<HistoricEvent>> getHistory(@PathVariable("id") String id) {
    Resources<HistoricEvent> history = new Resources<>(historyService.getHistoryForRequest(id));
    return new ResponseEntity<>(history, HttpStatus.OK);
  }
}
