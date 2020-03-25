package cern.modesti.config;

import cern.modesti.point.Point;
import cern.modesti.point.PointImpl;
import cern.modesti.request.history.RequestHistoryImpl;
import cern.modesti.schema.SchemaImpl;
import cern.modesti.schema.category.CategoryImpl;
import cern.modesti.schema.category.DatasourceImpl;
import cern.modesti.schema.field.*;
import cern.modesti.user.OidcUserImpl;
import cern.modesti.user.UserImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.validation.Validator;

/**
 * Configuration class for customising the usage of Spring Data REST.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableEntityLinks
public class RestConfig extends RepositoryRestConfigurerAdapter {

  @Autowired
  private Validator validator;

  @Override
  public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
    validatingListener.addValidator("beforeCreate", validator);
    validatingListener.addValidator("beforeSave", validator);
  }

  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    super.configureRepositoryRestConfiguration(config);

    // Tell Spring Data REST to expose IDs for the following classes in JSON responses.
    config.exposeIdsFor(RequestHistoryImpl.class, UserImpl.class, OidcUserImpl.class, SchemaImpl.class,
        CategoryImpl.class, DatasourceImpl.class, Field.class, TextField.class, AutocompleteField.class, NumericField.class,
        CheckboxField.class, OptionsField.class, Option.class, EmailField.class, DateField.class);

    config.setReturnBodyOnCreate(true);
    config.setReturnBodyOnUpdate(true);

    config.setBasePath("/api");
  }

  @Override
  public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
    SimpleModule module = new SimpleModule("CustomModule");
    module.addAbstractTypeMapping(Point.class, PointImpl.class);
    objectMapper.registerModule(module);
    // Register a deserialiser for Joda classes
    objectMapper.registerModule(new JodaModule());
  }
}