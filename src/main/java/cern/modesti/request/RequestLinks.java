package cern.modesti.repository.mongo.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import cern.modesti.model.Request;
import cern.modesti.repository.mongo.schema.Schema;

@Component
public class RequestLinks {

  @Autowired
  private EntityLinks entityLinks;

  Link getSchemaLink(Request request) {
    return entityLinks.linkToSingleResource(Schema.class, request.getSchema().getName());
  }
}
