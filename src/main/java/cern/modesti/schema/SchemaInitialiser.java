package cern.modesti.schema;

import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.processor.SchemaPostProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import sun.net.www.protocol.file.FileURLConnection;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.String.format;

/**
 * This class will delete all schemas stored in the schema repository and re-add them.
 *
 * Schemas are loaded from plugin JARs on the classpath.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class SchemaInitialiser {

  private static final String PLUGIN_RESOURCE_PREFIX      = "classpath*:/";
  private static final String SCHEMA_RESOURCE_PATTERN     = "schemas/*.json";
  private static final String DATASOURCE_RESOURCE_PATTERN = "schemas/datasources/*.json";
  private static final String CATEGORY_RESOURCE_PATTERN   = "schemas/categories/*.json";

  private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private ObjectMapper mapper;

  /**
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @PostConstruct
  public void init() throws IOException, URISyntaxException {
    log.info("Initialising schemas");

    schemaRepository.deleteAll();
    schemaRepository.save(loadSchemas());
  }


  /**
   * Load all schemas inside plugins.
   *
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  private List<Schema> loadSchemas() throws IOException, URISyntaxException {
    List<Schema> schemas = new ArrayList<>();

    Map<String, List<Schema>> pluginSchemas = loadPluginSchemas();
    Map<String, List<Category>> pluginCategories = loadPluginCategories();
    Map<String, List<Datasource>> pluginDatasources = loadPluginDatasources();

    for (Map.Entry<String, List<Schema>> entry : pluginSchemas.entrySet()) {
      String pathToJar = entry.getKey();

      for (Schema schema : entry.getValue()) {
        List<Category> categories = new ArrayList<>();
        List<Datasource> datasources = new ArrayList<>();

        // Skip over abstract schemas
        if (schema.isAbstract()) {
          continue;
        }

        // Merge in the parent schema if it is specified
        String parent = schema.getParent();
        if (parent != null) {

          Schema parentSchema = getSchema(pluginSchemas, parent);
          if (parentSchema == null) {
            throw new IllegalArgumentException(format("Schema %s extends from unknown parent schema %s", schema.getId(), parent));
          }

          categories.addAll(parentSchema.getCategories());
          datasources.addAll(parentSchema.getDatasources());
        }

        // Attach categories that are specified in the schema
        for (Category category : schema.getCategories()) {
          boolean categoryFound = false;

          for (Map.Entry<String, List<Category>> categoryEntry : pluginCategories.entrySet()) {
            if (categoryEntry.getValue().contains(category)) {
              categories.add(categoryEntry.getValue().get(categoryEntry.getValue().indexOf(category)));
              categoryFound = true;
            }
          }

          if (!categoryFound) {
            throw new IllegalArgumentException(format("Category %s was not found for schema %s", category.getId(), schema.getId()));
          }
        }

        // Attach datasources that are specified in the schema
        for (Datasource datasource : schema.getDatasources()) {
          boolean datasourceFound = false;

          for (Map.Entry<String, List<Datasource>> datasourceEntry : pluginDatasources.entrySet()) {
            if (datasourceEntry.getValue().contains(datasource)) {
              datasources.add(datasourceEntry.getValue().get(datasourceEntry.getValue().indexOf(datasource)));
              datasourceFound = true;
            }
          }

          if (!datasourceFound) {
            throw new IllegalArgumentException(format("Datasource %s was not found for schema %s", datasource.getId(), schema.getId()));
          }
        }

        // Process any overrides to the core categories
        for (Category override : schema.getOverrides()) {
          if (categories.contains(override)) {
            Category overridden = mergeCategories(categories.get(categories.indexOf(override)), override);
            categories.set(categories.indexOf(override), overridden);
          }
        }

        // Process any overrides to the core datasources
        for (Datasource override : schema.getDatasourceOverrides()) {
          if (datasources.contains(override)) {
            Category overridden = mergeCategories(datasources.get(datasources.indexOf(override)), override);
            datasources.set(datasources.indexOf(override), new Datasource(overridden));
          }
        }

        schema.setCategories(categories);
        schema.setDatasources(datasources);

        // Invoke any post processors
        Map<String, SchemaPostProcessor> postProcessors = applicationContext.getBeansOfType(SchemaPostProcessor.class);

        for (SchemaPostProcessor postProcessor : postProcessors.values()) {
          schema = postProcessor.postProcess(schema);
        }

        schemas.add(schema);
      }
    }

    log.trace(format("loaded %d schemas", schemas.size()));
    return schemas;
  }

  /**
   *
   * @param schemas
   * @param schemaId
   * @return
   */
  private Schema getSchema(Map<String, List<Schema>> schemas, String schemaId) {
    for (Map.Entry<String, List<Schema>> entry : schemas.entrySet()) {
      for (Schema schema : entry.getValue()) {
        if (schema.getId().equals(schemaId)) {
          return schema;
        }
      }
    }

    return null;
  }

  /**
   *
   * @param overridden
   * @param override
   * @return
   */
  private Category mergeCategories(Category overridden, Category override) {
    overridden = new Category(overridden);

    List<Field> newFields = new ArrayList<>();
    List<Field> oldFields = overridden.getFields();

    for (Field newField : override.getFields()) {
      if (!oldFields.contains(newField)) {
        newFields.add(newField);
      } else {
        oldFields.set(oldFields.indexOf(newField), mergeFields(oldFields.get(oldFields.indexOf(newField)), newField));
      }
    }

    // Copy the editable state list.
    if (override.getEditable() != null) {
      overridden.setEditable(override.getEditable());
    }

    // Copy the constraint list
    if (override.getConstraints() != null) {
      overridden.setConstraints(override.getConstraints());
    }

    overridden.getFields().addAll(newFields);
    return overridden;
  }

  /**
   *
   * @param overridden
   * @param override
   * @return
   */
  private Field mergeFields(Field overridden, Field override) {
    // Make a copy to avoid accidentally overriding the core schema
    Field copy = SerializationUtils.clone(overridden);
    BeanUtils.copyProperties(override, copy);
    return copy;
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
  private <T> Map<String, List<T>> loadPluginResources(String pattern, Class<T> klass) throws IOException, URISyntaxException {
    Map<String, List<T>> resources = new HashMap<>();

    for (Resource resource : resolver.getResources(pattern)) {
      // When running as a jar, don't detect ourselves as a plugin
      if (resource.getURI().toString().contains("modesti-server")) {
        continue;
      }

      T t = loadResource(resource, klass);

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
    URLConnection urlConnection = resource.getURL().openConnection();

    if (urlConnection instanceof JarURLConnection) {
      return new File(((JarURLConnection) urlConnection).getJarFileURL().toURI()).getAbsolutePath();
    } else {
      log.warn(format("loading resource outside of JAR file: %s", resource.getFile().getAbsolutePath()));

      if (resource.getFile().getParentFile().getName().equals("schemas")) {
        return resource.getFile().getParentFile().getAbsolutePath();
      } else {
        return resource.getFile().getParentFile().getParentFile().getAbsolutePath();
      }
    }
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
