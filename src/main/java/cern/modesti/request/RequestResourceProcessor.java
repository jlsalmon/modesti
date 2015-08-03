package cern.modesti.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import cern.modesti.schema.SchemaController;

import java.util.List;

/**
 * TODO
 *
 * Processes a single request resource, i.e. /requests/1
 *
 * @author Justin Lewis Salmon
 */
@Component
public class RequestResourceProcessor implements ResourceProcessor<Resource<Request>> {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaController.class);

  @Autowired
  RequestLinks requestLinks;

  @Override
  public Resource<Request> process(Resource<Request> resource) {
    Request request = resource.getContent();
    LOG.debug("adding links to request " + request.getRequestId());

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
