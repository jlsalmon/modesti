package cern.modesti.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.modesti.repository.request.schema.Schema;
import cern.modesti.repository.request.schema.SchemaRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

/**
 * This class will delete all schemas stored in the schema repository and re-add
 * them.
 * 
 * @author Justin Lewis Salmon
 *
 */
@Service
// @Profile("dev")
public class SchemaInitialiser {
  private static final Logger LOG = LoggerFactory.getLogger(SchemaInitialiser.class);

  private static final String BASE_PATH = "data/schemas/";

  private ObjectMapper mapper;

  private String[] schemas = { "core.json", "tim/tim.json", "tim/sources/plc.json" };

  @Autowired
  public SchemaInitialiser(SchemaRepository repo) throws IOException {
    LOG.info("Initialising schemas");
    mapper = new ObjectMapper();

    repo.deleteAll();

    for (String schema : schemas) {
      repo.insert(loadSchemaFromFile(BASE_PATH + schema));
    }
  }

  /**
   * 
   * @param path
   * @return
   * @throws IOException
   */
  public Schema loadSchemaFromFile(String path) throws IOException {
    LOG.info("Loading schema from classpath resource: " + path);
    byte[] bytes = ByteStreams.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
    return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), Schema.class);
  }
}
