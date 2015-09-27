package cern.modesti.request.search;

import cern.modesti.request.Request;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.RequestResourceAssembler;
import com.mysema.query.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class RequestSearchController {

  @Autowired
  private RequestRepository repository;

  @Autowired
  private RequestResourceAssembler resourceAssembler;

  @RequestMapping(value = "/requests/search", method = GET, produces = "application/json")
  HttpEntity<PagedResources<Request>> search(@RequestParam("query") String query, Pageable pageable, PagedResourcesAssembler assembler) {
    Page<Request> requests;

    if (!query.isEmpty()) {
      Predicate predicate = new RsqlExpressionBuilder<>(Request.class).createExpression(query);
      log.debug(predicate.toString());
      requests = repository.findAll(predicate, pageable);
    } else {
      requests = repository.findAll(pageable);
    }

    return new ResponseEntity<>(assembler.toResource(requests, resourceAssembler), HttpStatus.OK);
  }
}
