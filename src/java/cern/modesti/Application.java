package cern.modesti;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import cern.modesti.config.DataSourceConfig;
import cern.modesti.config.JpaConfig;
import cern.modesti.config.MongoConfig;
import cern.modesti.config.RestConfig;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@SpringBootApplication
@Import( {DataSourceConfig.class, JpaConfig.class, MongoConfig.class, RestConfig.class} )
public class Application {

  public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

//    SchemaRepository repo = context.getBean(SchemaRepository.class);
//
//    byte[] encoded = Files.readAllBytes(Paths.get("/home/jsalmon/afs/workspace/modesti/app/data/core-fields.json"));
//    String json = new String(encoded, StandardCharsets.UTF_8);
//
//    ObjectMapper mapper = new ObjectMapper();
//    Schema animal = mapper.readValue(json, Schema.class);
//
//    repo.insert(animal);
  }
}
