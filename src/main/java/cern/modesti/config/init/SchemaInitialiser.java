package cern.modesti.config.init;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import cern.modesti.schema.Schema;
import cern.modesti.repository.mongo.schema.SchemaRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

/**
 * This class will delete all schemas stored in the schema repository and re-add
 * them from the data/schemas classpath directory.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Profile({"test", "dev"})
public class SchemaInitialiser {
  private static final Logger LOG = LoggerFactory.getLogger(SchemaInitialiser.class);

  private static final String SCHEMA_RESOURCE_PATTERN = "classpath:/data/schemas/**/*.json";

  private ObjectMapper mapper;

  @Autowired
  public SchemaInitialiser(SchemaRepository repo) throws IOException {
    LOG.info("Initialising schemas");
    mapper = new ObjectMapper();

    repo.deleteAll();

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource[] schemas = resolver.getResources(SCHEMA_RESOURCE_PATTERN);

    for (Resource schema : schemas) {
      repo.insert(loadSchemaFromResource(schema));
    }
  }

  /**
   * @param resource
   *
   * @return
   *
   * @throws IOException
   */
  public Schema loadSchemaFromResource(Resource resource) throws IOException {
    LOG.info("Loading schema from classpath resource: " + resource);
    byte[] bytes = ByteStreams.toByteArray(resource.getInputStream());
    return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), Schema.class);
  }
}
