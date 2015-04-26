package cern.modesti.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import cern.modesti.model.Request;
import cern.modesti.repository.mongo.schema.SchemaController;

/**
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
    LOG.debug("adding schema link to request " + request.getRequestId());

    resource.add(requestLinks.getSchemaLink(request));
    return resource;
  }

}
