package cern.modesti.schema;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
@ExposesResourceFor(Schema.class)
public class SchemaController {

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
   * TODO fix the semantics of this method. Does passing "categories" set the list, or add them to the existing categories?
   *
   * GET /requests/{id}/schema?categories=logging
   *
   * @param id
   * @param categories
   * @return
   */
  @RequestMapping(value = "/requests/{id}/schema", method = GET)
  public HttpEntity<Resource<Schema>> getSchema(@PathVariable("id") String id, @RequestParam(value = "categories", required = false) String categories) {

    Request request = requestRepository.findOneByRequestId(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Parse the categories into a list
    Set<String> categoryList = new HashSet<>();
    // Add the existing categories
    categoryList.addAll(request.getCategories());

    if (categories != null && !categories.isEmpty()) {
      List<String> c = Arrays.asList(categories.split(","));
      categoryList.addAll(c);
    }

    // Generate a schema for the request, adding the newly given categories
    Schema schema = schemaService.materialiseSchema(request, categoryList);
    if (schema == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //request.setCategories(categoryList);
    //requestRepository.save(request);

    // Add the self link
    Resource<Schema> resource = new Resource<>(schema);
    resource.add(linkTo(methodOn(SchemaController.class).getSchema(id, categories)).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  void handleException(IllegalStateException e) {
    log.error("Caught exception: ", e);
  }
}