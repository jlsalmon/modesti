package cern.modesti.workflow.request;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import cern.modesti.request.Request;
import cern.modesti.request.RequestService;
import cern.modesti.user.User;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


/**
 * REST controller for retrieving and executing actions on workflow requests.
 *
 * @author Ivan Prieto Barreiro
 */
@RepositoryRestController
public class RequestController {
	
  @Autowired
  private RequestService requestService;

  @RequestMapping(path="/requests/{id}", value="/requests/{id}", method=POST, produces="application/hal+json")
  @PreAuthorize("permitAll")
  public HttpEntity<Resource<Request>> action(@PathVariable("id") String requestId, @RequestBody RequestAction action,
			Principal principal) {
    User user = (User) ((AbstractAuthenticationToken) principal).getPrincipal();
    Request request = requestService.execute(requestId, action, user);
    
    if (request != null) {
    	Resource<Request> resource = new Resource<>(request);
    	resource.add(linkTo(methodOn(RequestController.class).action(requestId, action, principal)).withSelfRel());
    	return new ResponseEntity<>(resource, HttpStatus.OK);
    }
    
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
