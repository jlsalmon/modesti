package cern.modesti.schema;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.schema.SchemaRepository;
import cern.modesti.request.Request;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Justin Lewis Salmon
 */
@Controller
@ExposesResourceFor(Schema.class)
public class SchemaController {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaController.class);

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private SchemaService schemaService;

  @Autowired
  private EntityLinks entityLinks;

  @RequestMapping(value = "/schemas", method = GET)
  public HttpEntity<Resources<Schema>> getSchemas() {
    Resources<Schema> resources = new Resources<>(schemaRepository.findAll());
    resources.add(entityLinks.linkToCollectionResource(Schema.class));

    return new ResponseEntity<>(resources, HttpStatus.OK);
  }

  /**
   * GET /requests/{id}/schema?categories=logging
   *
   * @param id
   * @param categories
   * @return
   */
  @RequestMapping(value = "/requests/{id}/schema", method = GET)
  public HttpEntity<Resource<Schema>> getSchema(@PathVariable("id") String id, @RequestParam(value = "categories", required = false) String categories) {

    // Parse the categories into a list
    List<String> categoryList;

    if (categories == null || categories.isEmpty()) {
      categoryList = new ArrayList<>();
    } else {
      categoryList = new ArrayList<>(Arrays.asList(categories.split(",")));
    }

    Request request = requestRepository.findOneByRequestId(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Schema schema = schemaService.materialiseSchema(request, categoryList);
    if (schema == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    request.setCategories(categoryList);
    requestRepository.save(request);

    Resource<Schema> resource = new Resource<>(schema);
    resource.add(linkTo(methodOn(SchemaController.class).getSchema(id, categories)).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  void handleException(IllegalStateException e) {
    LOG.error("Caught exception: ", e);

  }
}