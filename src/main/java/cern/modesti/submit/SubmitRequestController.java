/**
 *
 */
package cern.modesti.submit;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Justin Lewis Salmon
 *
 */
@Controller
public class SubmitRequestController {

  @Autowired
  SubmitRequestService service;

  @RequestMapping(value = "/requests/{id}/submit", method = POST)
  public void submitRequest(@PathVariable("id") String id) {
    service.submitRequest(id);
  }
}
