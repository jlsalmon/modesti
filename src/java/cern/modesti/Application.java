package cern.modesti;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import cern.modesti.config.DataSourceConfig;
import cern.modesti.config.JpaConfig;
import cern.modesti.config.MongoConfig;
import cern.modesti.config.RestConfig;
import cern.modesti.repository.request.schema.Schema;
import cern.modesti.repository.request.schema.SchemaRepository;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@Import( {DataSourceConfig.class, JpaConfig.class, MongoConfig.class, RestConfig.class} )
public class Application {

  public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

//    SchemaRepository repo = context.getBean(SchemaRepository.class);
//
//    byte[] encoded = Files.readAllBytes(Paths.get("/Users/jussy/workspace/modesti/app/data/schemas/core.json"));
//    String json = new String(encoded, StandardCharsets.UTF_8);
//
//    ObjectMapper mapper = new ObjectMapper();
//    Schema core = mapper.readValue(json, Schema.class);
//    
//    encoded = Files.readAllBytes(Paths.get("/Users/jussy/workspace/modesti/app/data/schemas/tim.json"));
//    json = new String(encoded, StandardCharsets.UTF_8);
//    
//    Schema tim = mapper.readValue(json, Schema.class);
//
//    repo.insert(core);
//    repo.insert(tim);
  }
}
