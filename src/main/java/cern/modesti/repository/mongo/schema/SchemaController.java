package cern.modesti.repository.mongo.schema;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/schemas")
@ExposesResourceFor(Schema.class)
public class SchemaController {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaController.class);

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private SchemaService schemaService;

  @Autowired
  private EntityLinks entityLinks;

  @RequestMapping(method = GET)
  HttpEntity<Resources<Schema>> getSchemas() {
    Resources<Schema> resources = new Resources<>(schemaRepository.findAll());
    resources.add(entityLinks.linkToCollectionResource(Schema.class));

    return new ResponseEntity<>(resources, HttpStatus.OK);
  }

  @RequestMapping(value = "/{name}", method = GET)
  HttpEntity<Resource<Schema>> getSchema(@PathVariable("name") String name) {

    // TODO delegate this to a SchemaService
    Schema schema = schemaService.materialiseSchema(name);

    if (schema == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Resource<Schema> resource = new Resource<>(schema);
    resource.add(linkTo(methodOn(SchemaController.class).getSchema(name)).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  @ExceptionHandler(IllegalStateException.class)
  void handleException(IllegalStateException e) {
    LOG.error("Caught exception: ", e);
  }
}