package cern.modesti.repository.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cern.modesti.repository.request.schema.Schema;

@Component
public class SchemaConverter implements Converter<Schema, String>{

  Logger logger = LoggerFactory.getLogger(SearchTextConverter.class);

  @Override
  public String convert(Schema schema) {
    // Merge the core schema with the domain-specific schema
    logger.info("converting schema");
    
    return "lol";
  }

}

