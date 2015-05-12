/**
 *
 */
package cern.modesti.workflow;

import cern.modesti.repository.jpa.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class WorkflowController {

  @Autowired
  WorkflowService service;

  /**
   *
   * @param id
   * @return
   */
  @RequestMapping(value = "/requests/{id}/validate", method = POST)
  HttpEntity<Resource<ValidationResult>> validate(@PathVariable("id") String id) {
    service.validateRequest(id);

//    Resource<ValidationResult> resource = new Resource<>(result);
//    resource.add(linkTo(methodOn(WorkflowController.class).validate(id, user)).withSelfRel());
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
