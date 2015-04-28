package cern.modesti.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import cern.modesti.schema.Schema;

@Component
public class RequestLinks {

  @Autowired
  private EntityLinks entityLinks;

  private static final Logger LOG = LoggerFactory.getLogger(RequestLinks.class);

  Link getSchemaLink(Request request) {
    if (request.getSchema() != null) {
      return entityLinks.linkToSingleResource(Schema.class, request.getSchema().getName());
    } else {
      LOG.warn("Request " + request.getRequestId() + " has no schema link!");
      return null;
    }
  }
}
