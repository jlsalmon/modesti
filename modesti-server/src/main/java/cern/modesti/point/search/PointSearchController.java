package cern.modesti.point.search;

import cern.modesti.plugin.spi.SearchProvider;
import cern.modesti.point.Point;
import cern.modesti.point.PointConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * REST controller for searching {@link Point} instances via RSQL queries.
 *
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class PointSearchController {

  @Autowired
  private PointResourceAssembler resourceAssembler;

  @Autowired
  private PointConverter pointConverter;

  @Autowired
  private ApplicationContext context;

  /**
   * From a given query string, parse an RSQL predicate expression and delegate
   * to an appropriate plugin to search for a list of points based on the
   * predicate.
   *
   * @param query the RSQL query string
   * @param pageable paging information
   * @param assembler helper for assembling pages of results
   * @return a page of {@link Point} instances and a HTTP status
   */
  @RequestMapping(value = "/api/points/search", method = GET, produces = "application/json")
  HttpEntity<PagedResources<Point>> search(@RequestParam("domain") String domain, @RequestParam("query") String query, Pageable pageable,
                                           PagedResourcesAssembler assembler) {

    SearchProvider searchProvider = getPluginSearchProvider(domain);

    if (searchProvider != null) {
      Page<Point> points = searchProvider.findAll(query, pageable, pointConverter);
      if (points == null) {
        points = new PageImpl<>(new ArrayList<>());
      }
      return new ResponseEntity<>(assembler.toResource(points, resourceAssembler), HttpStatus.OK);
    }
    else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  private SearchProvider getPluginSearchProvider(String requestPluginId) {
    for (SearchProvider searchProvider : context.getBeansOfType(SearchProvider.class).values()) {
      if (searchProvider.getPluginId().equals(requestPluginId)) {
        return searchProvider;
      }
    }
    return null;
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
