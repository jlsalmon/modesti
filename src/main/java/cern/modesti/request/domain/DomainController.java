package cern.modesti.request.domain;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.request.Request;
import cern.modesti.schema.Datasource;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@Controller
@Slf4j
public class DomainController {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;
  @Autowired
  private SchemaRepository schemaRepository;

  @RequestMapping(value = "/domains", method = GET)
  public HttpEntity<Resources<Domain>> getDomains() {

    List<RequestProvider> plugins = requestProviderRegistry.getPlugins();
    Set<Domain> domains = new HashSet<>();

    for (RequestProvider plugin : plugins) {
      Domain domain = new Domain(plugin.getMetadata().getName());

      Schema schema = schemaRepository.findOne(domain.getName());

      if (schema != null) {
        domain.setDatasources(schema.getDatasources().stream().map(Datasource::getId).collect(Collectors.toList()));
        domains.add(domain);
      }
    }


    // Return a list of Domain objects representing each of the current plugins and their associated datasources and categories.


    Resources<Domain> resources = new Resources<>(domains);

    return new ResponseEntity<>(resources, HttpStatus.OK);
  }
}
