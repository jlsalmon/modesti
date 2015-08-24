package cern.modesti.schema;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

  /**
   * TODO
   *
   * @param id
   * @return
   */
  @RequestMapping(value = "/requests/{id}/schema", method = GET)
  public HttpEntity<Resource<Schema>> getSchema(@PathVariable("id") String id) {

    Request request = requestRepository.findOneByRequestId(id);
    if (request == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Generate a schema for the request, adding the newly given categories
    Schema schema = schemaRepository.findOne(request.getDomain());
    if (schema == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Add the self link
    Resource<Schema> resource = new Resource<>(schema);
    resource.add(linkTo(methodOn(SchemaController.class).getSchema(id)).withSelfRel());

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }
}