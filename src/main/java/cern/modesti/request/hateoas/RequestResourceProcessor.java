package cern.modesti.request.hateoas;

import cern.modesti.request.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TODO
 *
 * Processes a single request resource, i.e. /requests/1
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class RequestResourceProcessor implements ResourceProcessor<Resource<Request>> {

  @Autowired
  RequestLinks requestLinks;

  @Override
  public Resource<Request> process(Resource<Request> resource) {
    Request request = resource.getContent();
    log.debug("adding links to request " + request.getRequestId());

    resource.add(requestLinks.getSchemaLink(request));

    List<Link> taskLinks = requestLinks.getTaskLinks(request);
    if (!taskLinks.isEmpty()) {
      resource.add(taskLinks);
    }

    List<Link> signalLinks = requestLinks.getSignalLinks(request);
    if (!signalLinks.isEmpty()) {
      resource.add(signalLinks);
    }

    return resource;
  }

}
