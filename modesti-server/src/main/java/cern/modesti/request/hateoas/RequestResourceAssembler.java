package cern.modesti.request.hateoas;

import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
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
public class RequestResourceAssembler extends ResourceAssemblerSupport<Request, Resource> {

  public RequestResourceAssembler() {
    super(RequestSearchController.class, Resource.class);
  }

  @Autowired
  private EntityLinks entityLinks;

  @Override
  public List<Resource> toResources(Iterable<? extends Request> requests) {
    List<Resource> resources = new ArrayList<>();

    for (Request request : requests) {
      Link self = entityLinks.linkToSingleResource(RequestImpl.class, request.getRequestId());
      resources.add(new Resource<>(request, self));
    }

    return resources;
  }

  @Override
  public Resource<Request> toResource(Request request) {
    Link self = entityLinks.linkToSingleResource(RequestImpl.class, request.getId()).withSelfRel();
    return new Resource<>(request, self);
  }
}
