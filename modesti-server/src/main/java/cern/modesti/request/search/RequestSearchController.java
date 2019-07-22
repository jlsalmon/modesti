package cern.modesti.request.search;

import cern.modesti.predicate.RsqlExpressionBuilder;
import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import cern.modesti.request.RequestProjection;
import cern.modesti.request.RequestRepository;
import cern.modesti.request.hateoas.RequestProjectionResourceAssembler;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * REST controller for searching {@link Request} instances via RSQL queries.
 *
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class RequestSearchController {

  @Autowired
  private RequestRepository repository;

  @Autowired
  private PagedResourcesAssembler<RequestProjection> assembler;
  
  @Autowired
  private RequestProjectionResourceAssembler projectionAssembler;

  /**
   * Searches for requests in the repository using the provided query (if any)
   * @param query Search query
   * @param pageable Pagination information
   * @return Page of requests fulfilling the search conditions
   */
  @RequestMapping(value = "/api/requests/search", method = GET, produces = "application/json")
  ResponseEntity<PagedResources<?>> search(@RequestParam("query") String query, Pageable pageable) {
    Page<RequestProjection> requests;

    if (!query.isEmpty()) {
      Predicate predicate = new RsqlExpressionBuilder<>(RequestImpl.class).createExpression(query);
      log.debug(predicate.toString());
      requests = repository.findAllProjectedBy(predicate, pageable);
    } else {
      requests = repository.findAllProjectedBy(pageable);
    }

    PagedResources<Resource> resources = assembler.toResource(requests, projectionAssembler);
    return ResponseEntity.ok(resources);
  }
}
