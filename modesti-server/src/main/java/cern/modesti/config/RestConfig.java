package cern.modesti.config;

import cern.modesti.point.PointImpl;
import cern.modesti.request.Request;
import cern.modesti.request.RequestDeserialiser;
import cern.modesti.point.Point;
import cern.modesti.request.RequestImpl;
import cern.modesti.request.history.RequestHistoryImpl;
import cern.modesti.schema.Schema;
import cern.modesti.schema.SchemaImpl;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.CategoryImpl;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.category.DatasourceImpl;
import cern.modesti.schema.field.*;
import cern.modesti.user.User;
import cern.modesti.user.UserImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    config.exposeIdsFor(RequestImpl.class, PointImpl.class, RequestHistoryImpl.class, UserImpl.class, SchemaImpl.class,
        CategoryImpl.class, DatasourceImpl.class, Field.class, TextField.class, AutocompleteField.class, NumericField.class,
        CheckboxField.class, OptionsField.class, Option.class);

    config.setReturnBodyOnCreate(true);
    config.setReturnBodyOnUpdate(true);

    config.setBasePath("/api");
  }

  @Override
  public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
    // Custom deserialiser for {@link Request} objects
    objectMapper.registerModule(new SimpleModule("RequestModule") {
      @Override
      public void setupModule(SetupContext context) {
        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(Request.class, new RequestDeserialiser());
        context.addDeserializers(deserializers);
      }
    });
  }
}
