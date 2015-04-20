package cern.modesti.repository.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import cern.modesti.model.Request;

/**
 *
 * @author Justin Lewis Salmon
 */
@Component
public class RequestResourceProcessor implements ResourceProcessor<Resource<Request>> {

  @Autowired
  RequestLinks requestLinks;

  @Override
  public Resource<Request> process(Resource<Request> resource) {
    System.out.println("processing");
    Request request = resource.getContent();
    resource.add(requestLinks.getSchemaLink(request));
    return resource;
  }

}
