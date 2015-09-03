package cern.modesti.point;

import cern.modesti.repository.point.RefPoint;
import cern.modesti.repository.point.RefPointRepository;
import com.mysema.query.types.expr.BooleanExpression;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class PointController {

  @Autowired
  private PointRepository repository;

  @Autowired
  private PointResourceAssembler pointResourceAssembler;

  @RequestMapping(value = "/points", method = GET, produces = "application/json")
  HttpEntity<PagedResources<Point>> getPointsByRsql(@RequestParam("search") String search, Pageable pageable, PagedResourcesAssembler assembler) {

    final Node rootNode = new RSQLParser().parse(search);
    final BooleanExpression predicate = rootNode.accept(new CustomRsqlVisitor());
    log.debug(predicate.toString());

    Page<Point> points = repository.findAll(predicate, pageable);

    return new ResponseEntity<>(assembler.toResource(points, pointResourceAssembler), HttpStatus.OK);
  }

  @Component
  public static class PointResourceAssembler extends ResourceAssemblerSupport<Point, Resource> {

    public PointResourceAssembler() {
      super(PointController.class, Resource.class);
    }

    @Override
    public List<Resource> toResources(Iterable<? extends Point> points) {
      List<Resource> resources = new ArrayList<>();

      for(Point point : points) {
        resources.add(new Resource<>(point));
      }

      return resources;
    }

    @Override
    public Resource toResource(Point point) {
      return new Resource<>(point);
    }
  }
}
