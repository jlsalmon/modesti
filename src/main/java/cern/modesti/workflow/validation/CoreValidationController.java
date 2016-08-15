package cern.modesti.workflow.validation;

import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Justin Lewis Salmon
 */
@Controller
public class CoreValidationController {

  @Autowired
  private CoreValidationService validationService;

  @Autowired
  private RequestService requestService;


  @RequestMapping(value = "/api/requests/{id}/validate", method = POST)
  public HttpEntity<Resource<Request>> validate(@PathVariable("id") String id) {
    Request request = requestService.findOneByRequestId(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    validationService.validateRequest(request);

    Resource<Request> resource = new Resource<>(request);
    return new ResponseEntity<>(resource, HttpStatus.OK);
  }
}
