package cern.modesti.request.hateoas;

import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import cern.modesti.request.RequestProjection;
import cern.modesti.request.search.RequestSearchController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for manually assembling {@link Request} instances
 * into {@link Resource} representations.
 *
 * @author Justin Lewis Salmon
 */
@Component
public class RequestProjectionResourceAssembler extends ResourceAssemblerSupport<RequestProjection, Resource> {

  public RequestProjectionResourceAssembler() {
    super(RequestSearchController.class, Resource.class);
  }

  @Autowired
  private EntityLinks entityLinks;

  @Override
  public List<Resource> toResources(Iterable<? extends RequestProjection> projections) {
    List<Resource> resources = new ArrayList<>();
    
    for (RequestProjection projection : projections) {
      resources.add(toResource(projection));
    }
    return resources;
  }
  
  @Override
  public Resource toResource(RequestProjection entity) {
    Link self = entityLinks.linkToSingleResource(RequestImpl.class, entity.getRequestId()).withSelfRel();
    return new Resource<>(entity, self);
  }
}
