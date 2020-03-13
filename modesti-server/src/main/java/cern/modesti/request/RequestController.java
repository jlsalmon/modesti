package cern.modesti.request;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.RequestService;
import cern.modesti.request.hateoas.RequestResourceAssembler;
import cern.modesti.user.User;
import cern.modesti.workflow.request.RequestAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.security.Principal;
import java.util.List;


/**
 * REST controller for handling requests.
 *
 * @author Ivan Prieto Barreiro
 */
@BasePathAwareController
public class RequestController {
        
  @Autowired
  private RequestService requestService;
  
  @Autowired
  private RequestRepository repository;
  
  @Autowired
  private RequestResourceAssembler resourceAssembler;
  
  /**
   * Gets the list of requests
   * @return List of requests from the repository
   */
  @GetMapping(path="/requests")
  @ResponseBody
  public HttpEntity<List<Resource>> getRequests() {
    List<? extends Request> requests = repository.findAll();
    return ResponseEntity.ok(resourceAssembler.toResources(requests));
  }
  
  /**
   * Inserts a new request in the repository.
   * @param request The request to insert.
   * @return
   */
  @PostMapping(path="/requests")
  @ResponseBody
  public HttpEntity<Resource<Request>> insertRequest(@RequestBody RequestImpl request) {
    Request saved = requestService.insert(request); 
    if (saved != null) {
      Resource<Request> resource = resourceAssembler.toResource(saved);
      return ResponseEntity.ok(resource);
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
  
  /**
   * Gets a request from the repository
   * @param id The request id
   * @return HttpEntity with the found request or NOT_FOUND otherwise
   */
  @GetMapping(path="/requests/{id}")
  public HttpEntity<Resource<Request>> getRequest(@PathVariable("id") String id) {
    Request request = repository.findOneByRequestId(id);

    if(request!=null) {
      Resource<Request> resource = resourceAssembler.toResource(request);
      return ResponseEntity.ok(resource);
    }
    
    return new ResponseEntity<> (HttpStatus.NOT_FOUND);
  }
  
  /**
   * Changes the request creator
   * @param requestId The request id
   * @param action the request action
   * @param principal the user who submits the change request
   * @return HttpEntity with the request
   */
  @PostMapping(path="/requests/{id}")
  public HttpEntity<Resource<Request>> assignCreator(@PathVariable("id") String requestId,
      @RequestBody RequestAction action, Principal principal) {
    User user = (User) ((AbstractAuthenticationToken) principal).getPrincipal();
    requestService.execute(requestId, action, user);
    return getRequest(requestId);
  }
  
  /**
   * Updates an existing request
   * @param id The request identifier
   * @param request The contents of the request to save
   * @return HttpEntity with the saved request
   */
  @PutMapping(path="/requests/{id}")
  @ResponseBody
  public HttpEntity<Resource<Request>> updateRequest(@PathVariable("id") String id, @RequestBody RequestImpl request) {
    Request orig = repository.findOneByRequestId(id);
    if (orig == null) {
      throw new RequestNotFoundException(id);
    }
    
    request.setId(orig.getId());
    Request saved = requestService.save(request);
    if (saved != null) {
      Resource<Request> resource = resourceAssembler.toResource(request);
      return ResponseEntity.ok(resource);
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
  
  /**
   * Deletes a request from the repository
   * @param id The request id
   */
  @DeleteMapping(path="/requests/{id}")
  public ResponseEntity<Void> deleteRequest(@PathVariable("id") String id) {
    Request request = repository.findOneByRequestId(id);
    if (request == null) {
      throw new RequestNotFoundException(id);
    }
    
    requestService.delete(request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
