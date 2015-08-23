package cern.modesti.config.init;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.modesti.plugin.RequestProvider;
import cern.modesti.request.Request;
import cern.modesti.schema.Datasource;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.schema.category.Category;
import com.fasterxml.jackson.core.JsonParseException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

import cern.modesti.schema.Schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import static java.lang.String.format;

/**
 * This class will delete all schemas stored in the schema repository and re-add them.
 *
 * Schemas are loaded from inside the application and from any plugin JARs on the classpath.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
@Profile({"test", "dev", "prod"})
public class SchemaInitialiser {

  private static final String SCHEMAS = "classpath*:/schemas";
  private static final String DATASOURCES = "/datasources";
  private static final String CATEGORIES  = "/categories";
  private static final String SUFFIX = "/*.json";

  private ObjectMapper mapper;
  private ResourcePatternResolver resolver;

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  public SchemaInitialiser(SchemaRepository repo) throws IOException, URISyntaxException {
    log.info("Initialising schemas");
    mapper = new ObjectMapper();
    resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

    repo.deleteAll();
    repo.insert(loadSchemas());
  }


  /**
   * Discover default schemas and all schemas inside plugins.
   *
   * @return
   * @throws IOException
   */
  private List<Schema> loadSchemas() throws IOException, URISyntaxException {
    List<Schema> schemas = new ArrayList<>();

    Map<String, Category> categories = loadCategories();
    Map<String, Datasource> datasources = loadDatasources();

    for (Resource schemaResource : resolver.getResources(SCHEMAS + SUFFIX)) {
      log.info("Loading schema from classpath resource: " + schemaResource);
      Schema schema = loadResource(schemaResource, Schema.class);

      String path = schemaResource.getURI().getSchemeSpecificPart();
      String parent;

      if (path.contains(".jar")) {
        JarURLConnection connection = (JarURLConnection) schemaResource.getURL().openConnection();
        parent = new File(connection.getJarFileURL().toURI()).getAbsolutePath();
      } else {
        parent = path.substring(0, path.lastIndexOf(File.separator));
      }

      for (Map.Entry<String, Category> entry : categories.entrySet()) {
        if (entry.getKey().equals(parent) && schema.getCategories().contains(entry.getValue())) {
          schema.getCategories().add(entry.getValue());
        }
      }

      for (Map.Entry<String, Datasource> entry : datasources.entrySet()) {
        if (entry.getKey().equals(parent) && schema.getDatasources().contains(entry.getValue())) {
          schema.getDatasources().add(entry.getValue());
        }
      }

      schemas.add(schema);
    }

    log.trace(format("loaded %d schemas", schemas.size()));
    return schemas;
  }

  /**
   *
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, Category> loadCategories() throws IOException, URISyntaxException {
    Map<String, Category> categories = new HashMap<>();

    for (Resource resource : resolver.getResources(SCHEMAS + CATEGORIES + SUFFIX)) {
      String filename = resource.getURI().getSchemeSpecificPart();

      if (filename.contains(".jar")) {
        JarURLConnection connection = (JarURLConnection) resource.getURL().openConnection();
        String jar = new File(connection.getJarFileURL().toURI()).getAbsolutePath();
        categories.put(jar, loadCategory(resource));
      } else {
        categories.put(resource.getURI().resolve("..").getSchemeSpecificPart(), loadCategory(resource));
      }
    }

    return categories;
  }

  /**
   * @return
   * @throws IOException
   */
  private Category loadCategory(Resource resource) throws IOException {
    log.info("Loading category from classpath resource: " + resource);
    return loadResource(resource, Category.class);
  }

  /**
   *
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, Datasource> loadDatasources() throws IOException, URISyntaxException {
    Map<String, Datasource> datasources = new HashMap<>();

    for (Resource resource : resolver.getResources(SCHEMAS + DATASOURCES + SUFFIX)) {
      String filename = resource.getURI().getSchemeSpecificPart();

      if (filename.contains(".jar")) {
        JarURLConnection connection = (JarURLConnection) resource.getURL().openConnection();
        String jar = new File(connection.getJarFileURL().toURI()).getAbsolutePath();
        datasources.put(jar, loadDatasource(resource));
      } else {
        datasources.put(resource.getURI().resolve("..").getSchemeSpecificPart(), loadDatasource(resource));
      }
    }

    return datasources;
  }

  /**
   * @return
   * @throws IOException
   */
  private Datasource loadDatasource(Resource resource) throws IOException {
    log.info("Loading datasource from classpath resource: " + resource);
    return loadResource(resource, Datasource.class);
  }

  /**
   * @param resource
   * @param klass
   * @param <T>
   * @return
   * @throws IOException
   */
  private <T> T loadResource(Resource resource, Class<T> klass) throws IOException {
    byte[] bytes = ByteStreams.toByteArray(resource.getInputStream());
    return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), klass);
  }
}
