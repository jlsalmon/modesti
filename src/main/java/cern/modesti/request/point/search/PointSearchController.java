package cern.modesti.request.point.search;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class PointSearchController {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private PointResourceAssembler resourceAssembler;

  /**
   * From a given query string, parse a RSQL predicate expression and delegate to an appropriate plugin to search for a list of points based on the predicate.
   *
   * @param query
   * @param pageable
   * @param assembler
   * @return
   */
  @RequestMapping(value = "/points/search", method = GET, produces = "application/json")
  HttpEntity<PagedResources<Point>> search(@RequestParam("domain") String domain, @RequestParam("query") String query, Pageable pageable, PagedResourcesAssembler assembler) {
    RequestProvider plugin = null;

    for (RequestProvider provider : requestProviderRegistry.getPlugins()) {
      if (provider.getMetadata().getName().equals(domain)) {
        plugin = provider;
      }
    }

    if (plugin == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    Page<Point> points = plugin.findAll(query, pageable);
    if (points == null) {
      points = new PageImpl<>(new ArrayList<>());
    }

    return new ResponseEntity<>(assembler.toResource(points, resourceAssembler), HttpStatus.OK);
  }

  @Component
  public static class PointResourceAssembler extends ResourceAssemblerSupport<Point, Resource> {

    public PointResourceAssembler() {
      super(PointSearchController.class, Resource.class);
    }

    @Override
    public List<Resource> toResources(Iterable<? extends Point> points) {
      List<Resource> resources = new ArrayList<>();

      for (Point point : points) {
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
