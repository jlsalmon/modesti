package cern.modesti.refpoint;

import cern.modesti.repository.refpoint.RefPoint;
import cern.modesti.repository.refpoint.RefPointRepository;
import cern.modesti.request.search.RsqlExpressionBuilder;
import com.mysema.query.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class RefPointController {

  @Autowired
  private RefPointRepository repository;

  @Autowired
  private RefPointResourceAssembler resourceAssembler;

  @RequestMapping(value = "/points/ref", method = GET, produces = "application/json")
  HttpEntity<PagedResources<RefPoint>> getPointsByRsql(@RequestParam("search") String search, Pageable pageable, PagedResourcesAssembler assembler) {

    Predicate predicate = new RsqlExpressionBuilder<>(RefPoint.class).createExpression(search);
    log.debug(predicate.toString());

    Page<RefPoint> points = repository.findAll(predicate, pageable);
    return new ResponseEntity<>(assembler.toResource(points, resourceAssembler), HttpStatus.OK);
  }

  @Component
  public static class RefPointResourceAssembler extends ResourceAssemblerSupport<RefPoint, Resource> {

    public RefPointResourceAssembler() {
      super(RefPointController.class, Resource.class);
    }

    @Override
    public List<Resource> toResources(Iterable<? extends RefPoint> points) {
      List<Resource> resources = new ArrayList<>();

      for(RefPoint point : points) {
        resources.add(new Resource<>(point));
      }

      return resources;
    }

    @Override
    public Resource toResource(RefPoint point) {
      return new Resource<>(point);
    }
  }
}
