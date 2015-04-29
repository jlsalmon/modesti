package cern.modesti.request;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import cern.modesti.schema.SchemaController;

@Component
public class RequestLinks {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLinks.class);

  Link getSchemaLink(Request request) {
    if (request.getCategories() != null) {

      // Need to manually build a comma-separated list of categories
      StringBuilder categories = new StringBuilder();
      for (String category : request.getCategories()) {
        categories.append(category).append(",");
      }

      if (categories.length() > 0) {
        categories.deleteCharAt(categories.length() - 1);
      }

      return linkTo(methodOn(SchemaController.class).getSchema(request.getRequestId(), categories.toString())).withRel("schema");
    } else {
      LOG.warn("Request " + request.getRequestId() + " has no schema link!");
      return null;
    }
  }
}
