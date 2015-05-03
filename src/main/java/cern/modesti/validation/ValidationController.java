package cern.modesti.validation;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import cern.modesti.repository.jpa.validation.ValidationResult;

/**
 *
 * @author Justin Lewis Salmon
 */
@Controller
@ExposesResourceFor(ValidationResult.class)
public class ValidationController {

  private static final Logger LOG = LoggerFactory.getLogger(ValidationController.class);

  @Autowired
  private EntityLinks entityLinks;


  @RequestMapping(value = "/requests/{id}/validate", method = GET)
  HttpEntity<Resource<ValidationResult>> validate(@PathVariable("id") Long id) {
    LOG.info("validating request " + id);
    ValidationResult result = new ValidationResult();
    Resource<ValidationResult> resource = new Resource<>(result);
    resource.add(linkTo(methodOn(ValidationController.class).validate(id)).withSelfRel());
    return new ResponseEntity<>(resource, HttpStatus.OK);
  }
}