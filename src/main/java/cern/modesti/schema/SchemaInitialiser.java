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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

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
 * This class is responsible for loading schemas from inside plugins. Schemas
 * must be placed in a {@literal schemas} directory on the classpath and be
 * JSON files.
 * <p>
 * Categories referred to by ID in the schema must have a corresponding file
 * in the {@literal schemas/categories} directory.
 * <p>
 * <b>Note:</b> at boot time, all schemas will be deleted and reloaded from the
 * classpath.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class SchemaInitialiser {

  private static final String PLUGIN_RESOURCE_PREFIX = "classpath*:/";
  private static final String SCHEMA_RESOURCE_PATTERN = "schemas/*.json";
  private static final String DATASOURCE_RESOURCE_PATTERN = "schemas/datasources/*.json";
  private static final String CATEGORY_RESOURCE_PATTERN = "schemas/categories/*.json";

  private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

  @Autowired
  private SchemaRepository schemaRepository;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private ObjectMapper mapper;

  @PostConstruct
  public void init() throws IOException, URISyntaxException {
    log.info("Initialising schemas");

    schemaRepository.deleteAll();
    schemaRepository.save(loadSchemas());
  }


  /**
   * Load all schemas inside plugins.
   *
   * @return the list of schemas that were found on the classpath
   * @throws IOException
   * @throws URISyntaxException
   */
  private List<Schema> loadSchemas() throws IOException, URISyntaxException {
    List<Schema> schemas = new ArrayList<>();

    Map<String, List<Schema>> pluginSchemas = loadPluginSchemas();
    Map<String, List<Category>> pluginCategories = loadPluginCategories();
    Map<String, List<Datasource>> pluginDatasources = loadPluginDatasources();

    for (Map.Entry<String, List<Schema>> entry : pluginSchemas.entrySet()) {
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

          schema.setIdProperty(parentSchema.getIdProperty());
          schema.setSelectableStates(parentSchema.getSelectableStates());
          schema.setRowCommentStates(parentSchema.getRowCommentStates());
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

          // TODO: support referencing another field by name to avoid duplicate field definitions
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
   * Merge two {@link Category} instances, copying the fields defined in the
   * overriding category to the overridden category.
   *
   * @param overridden the base category to merge into
   * @param override   the overriding category to merge from
   * @return a new category containing the result of the merge
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
   * Merge two {@link Field} instances, copying the fields defined in the
   * overriding field to the overridden field.
   *
   * @param overridden the base field to merge into
   * @param override   the overriding field to merge from
   * @return a new field containing the result of the merge
   */
  private Field mergeFields(Field overridden, Field override) {
    // Make a copy to avoid accidentally overriding the core schema
    Field copy = SerializationUtils.clone(overridden);
    BeanUtils.copyProperties(override, copy);
    return copy;
  }

  /**
   * Load all schema files from the classpath.
   *
   * @return a map of loaded schemas, keyed by absolute path to the file
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, List<Schema>> loadPluginSchemas() throws IOException, URISyntaxException {
    log.info("Loading plugin schemas");
    return loadPluginResources(PLUGIN_RESOURCE_PREFIX + SCHEMA_RESOURCE_PATTERN, Schema.class);
  }

  /**
   * Load all category files from the classpath.
   *
   * @return a map of loaded categories, keyed by absolute path to the file
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, List<Category>> loadPluginCategories() throws IOException, URISyntaxException {
    log.info("Loading plugin categories");
    return loadPluginResources(PLUGIN_RESOURCE_PREFIX + CATEGORY_RESOURCE_PATTERN, Category.class);
  }

  /**
   * Load all datasource files from the classpath.
   *
   * @return a map of loaded datasources, keyed by absolute path to the file
   * @throws IOException
   * @throws URISyntaxException
   */
  private Map<String, List<Datasource>> loadPluginDatasources() throws IOException, URISyntaxException {
    log.info("Loading plugin datasources");
    return loadPluginResources(PLUGIN_RESOURCE_PREFIX + DATASOURCE_RESOURCE_PATTERN, Datasource.class);
  }

  /**
   * Load a set of resources from the classpath that match the given resource
   * pattern,
   *
   * @param pattern an Ant-style path pattern
   * @param klass   the type of resource to be loaded
   * @param <T>     the type of resource to be loaded
   * @return a map of loaded resources, keyed by absolute path to the file
   * @throws IOException
   */
  private <T> Map<String, List<T>> loadPluginResources(String pattern, Class<T> klass) throws IOException, URISyntaxException {
    Map<String, List<T>> resources = new HashMap<>();

    for (Resource resource : resolver.getResources(pattern)) {
      T t = loadResource(resource, klass);

      String pathToResource = getAbsolutePathToResource(resource);

      if (resources.containsKey(pathToResource)) {
        resources.get(pathToResource).add(t);
      } else {
        resources.put(pathToResource, new ArrayList<>(Collections.singleton(t)));
      }
    }

    return resources;
  }

  /**
   * Retrieve the absolute path to a resource. The resource may be inside a JAR
   * or in an expanded classpath directory.
   *
   * @param resource the resource for which to fetch the path
   * @return the absolute path to the resource
   * @throws IOException
   * @throws URISyntaxException
   */
  private String getAbsolutePathToResource(Resource resource) throws IOException, URISyntaxException {
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
   * Load a resource from a file and deserialise it as a JSON string to the
   * given type.
   *
   * @param resource the resource to load
   * @param klass    the type to deserialise to
   * @param <T>      the type to deserialise to
   * @return the loaded resource, deserialised to the given type
   * @throws IOException
   */
  private <T> T loadResource(Resource resource, Class<T> klass) throws IOException {
    byte[] bytes = ByteStreams.toByteArray(resource.getInputStream());
    return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), klass);
  }

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
}
