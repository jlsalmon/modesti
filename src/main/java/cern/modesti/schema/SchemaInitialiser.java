package cern.modesti.schema;

import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.options.OptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.String.format;

/**
 * This class will delete all schemas stored in the schema repository and re-add them.
 *
 * Schemas are loaded from inside the application and from any plugin JARs on the classpath.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
@Profile({"dev", "prod"})
public class SchemaInitialiser {

  private static final String BUILTIN_RESOURCE_PREFIX     = "classpath*:/";
  private static final String PLUGIN_RESOURCE_PREFIX      = "classpath*:/";
  private static final String SCHEMA_RESOURCE_PATTERN     = "schemas/*.json";
  private static final String DATASOURCE_RESOURCE_PATTERN = "schemas/datasources/*.json";
  private static final String CATEGORY_RESOURCE_PATTERN   = "schemas/categories/*.json";

  private ObjectMapper mapper;
  private ResourcePatternResolver resolver;

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private OptionService optionService;

  /**
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @PostConstruct
  public void init() throws IOException, URISyntaxException {
    log.info("Initialising schemas");
    mapper = new ObjectMapper();
    resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

    schemaRepository.deleteAll();
    schemaRepository.save(loadSchemas());
  }


  /**
   * Load default schemas and all schemas inside plugins.
   *
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private List<Schema> loadSchemas() throws IOException, URISyntaxException {
    List<Schema> schemas = new ArrayList<>();

    List<Category> builtinCategories = loadBuiltinCategories();
    List<Datasource> builtinDatasources = loadBuiltinDatasources();

    Map<String, List<Schema>> pluginSchemas = loadPluginSchemas();
    Map<String, List<Category>> pluginCategories = loadPluginCategories();
    Map<String, List<Datasource>> pluginDatasources = loadPluginDatasources();

    // Load the built-in schemas
    List<Schema> builtinSchemas = loadBuiltinSchemas();
    schemas.addAll(builtinSchemas);

    // TODO this will break if more builtin schemas are added
    Schema core = builtinSchemas.get(0);

    // Attach the categories and datasources specified in the plugin schemas.
    for (Map.Entry<String, List<Schema>> entry : pluginSchemas.entrySet()) {
      String pathToJar = entry.getKey();

      for (Schema schema : entry.getValue()) {
        List<Category> categories = new ArrayList<>();

        // Attach the core categories
        categories.addAll(core.getCategories());

        // Attach categories (either built-in or from the plugin) that are specified in the schema
        for (Category category : schema.getCategories()) {
          if (pluginCategories.get(pathToJar) != null && pluginCategories.get(pathToJar).contains(category)) {
            categories.add(pluginCategories.get(pathToJar).get(pluginCategories.get(pathToJar).indexOf(category)));
          } else if (builtinCategories.contains(category)) {
            categories.add(builtinCategories.get(builtinCategories.indexOf(category)));
          } else {
            throw new IllegalArgumentException(format("Category %s was not found for schema %s", category.getId(), schema.getId()));
          }
        }

        // Attach datasources (either built-in or from the plugin) that are specified in the schema
        List<Datasource> datasources = new ArrayList<>();

        for (Datasource datasource : schema.getDatasources()) {
          if (pluginDatasources.get(pathToJar) != null && pluginDatasources.get(pathToJar).contains(datasource)) {
            datasources.add(pluginDatasources.get(pathToJar).get(pluginDatasources.get(pathToJar).indexOf(datasource)));
          } else if (builtinDatasources.contains(datasource)) {
            datasources.add(builtinDatasources.get(builtinDatasources.indexOf(datasource)));
          } else {
            throw new IllegalArgumentException(format("Datasource %s was not found for schema %s", datasource.getId(), schema.getId()));
          }
        }

        // Process any overrides to the core schema
        for (Category override : schema.getOverrides()) {
          Category overridden = new Category(categories.get(categories.indexOf(override)));

          List<Field> newFields = new ArrayList<>();

          for (Field newField : override.getFields()) {
            if (!overridden.getFields().contains(newField)) {
              newFields.add(newField);
            }
          }

          // Copy the disabled state list if the child doesn't specify it.
          if (override.getDisabledStates() != null && overridden.getDisabledStates() == null) {
            overridden.setDisabledStates(override.getDisabledStates());
          }

          // Copy the editable state list if the child doesn't specify it.
          if (override.getEditableStates() != null && overridden.getEditableStates() == null) {
            overridden.setEditableStates(override.getEditableStates());
          }

          // Copy the constraint list
          if (override.getConstraints() != null && overridden.getConstraints() == null) {
            overridden.setConstraints(override.getConstraints());
          }

          overridden.getFields().addAll(newFields);
          categories.set(categories.indexOf(override), overridden);
        }

        schema.setCategories(categories);
        schema.setDatasources(datasources);

        // Inject options
        optionService.injectOptions(schema);

        schemas.add(schema);
      }
    }

    log.trace(format("loaded %d schemas", schemas.size()));
    return schemas;
  }

  /**
   *
   * @return
   * @throws IOException
   */
  private List<Schema> loadBuiltinSchemas() throws IOException {
    log.info("Loading builtin schemas");
    return loadBuiltinResources(BUILTIN_RESOURCE_PREFIX + SCHEMA_RESOURCE_PATTERN, Schema.class);
  }

  /**
   *
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, List<Schema>> loadPluginSchemas() throws IOException, URISyntaxException {
    log.info("Loading plugin schemas");
    return loadPluginResources(PLUGIN_RESOURCE_PREFIX + SCHEMA_RESOURCE_PATTERN, Schema.class);
  }

  /**
   *
   * @return
   * @throws IOException
   */
  private List<Category> loadBuiltinCategories() throws IOException {
    log.info("Loading builtin categories");
    return loadBuiltinResources(BUILTIN_RESOURCE_PREFIX + CATEGORY_RESOURCE_PATTERN, Category.class);
  }

  /**
   *
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, List<Category>> loadPluginCategories() throws IOException, URISyntaxException {
    log.info("Loading plugin categories");
    return loadPluginResources(PLUGIN_RESOURCE_PREFIX + CATEGORY_RESOURCE_PATTERN, Category.class);
  }

  /**
   *
   * @return
   * @throws IOException
   */
  private List<Datasource> loadBuiltinDatasources() throws IOException {
    log.info("Loading builtin datasources");
    return loadBuiltinResources(BUILTIN_RESOURCE_PREFIX + DATASOURCE_RESOURCE_PATTERN, Datasource.class);
  }

  /**
   *
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, List<Datasource>> loadPluginDatasources() throws IOException, URISyntaxException {
    log.info("Loading plugin datasources");
    return loadPluginResources(PLUGIN_RESOURCE_PREFIX + DATASOURCE_RESOURCE_PATTERN, Datasource.class);
  }

  /**
   *
   * @param pattern
   * @param klass
   * @param <T>
   * @return
   * @throws IOException
   */
  private <T> List<T> loadBuiltinResources(String pattern, Class<T> klass) throws IOException {
    List<T> resources = new ArrayList<>();

    for (Resource resource : resolver.getResources(pattern)) {
      if (!resource.getURI().toString().contains("plugins")) {
        resources.add(loadResource(resource, klass));
      }
    }

    return resources;
  }

  /**
   *
   * @param pattern
   * @param klass
   * @param <T>
   * @return
   * @throws IOException
   */
  private <T> Map<String, List<T>> loadPluginResources(String pattern, Class<T> klass) throws IOException, URISyntaxException {
    Map<String, List<T>> resources = new HashMap<>();

    for (Resource resource : resolver.getResources(pattern)) {
      if (!resource.getURI().getSchemeSpecificPart().contains(".jar")) {
        continue;
      }

      T t = loadResource(resource, klass);

      // When running as a jar, dont detect ourselves as a plugin
      if (resource.getURI().toString().contains("modesti-api")) {
        continue;
      }

      String pathToJar = getPathToJar(resource);

      if (resources.containsKey(pathToJar)) {
        resources.get(pathToJar).add(t);
      } else {
        resources.put(pathToJar, new ArrayList<>(Collections.singleton(t)));
      }
    }

    return resources;
  }

  /**
   *
   * @param resource
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private String getPathToJar(Resource resource) throws IOException, URISyntaxException {
    return new File(((JarURLConnection) resource.getURL().openConnection()).getJarFileURL().toURI()).getAbsolutePath();
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
