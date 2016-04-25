package cern.modesti.config;

import cern.modesti.request.Request;
import cern.modesti.request.RequestDeserialiser;
import cern.modesti.request.point.Point;
import cern.modesti.schema.Schema;
import cern.modesti.schema.category.Category;
import cern.modesti.schema.category.Datasource;
import cern.modesti.schema.field.*;
import cern.modesti.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
    config.exposeIdsFor(Point.class, Field.class, TextField.class, OptionsField.class, AutocompleteField.class, NumericField.class, CheckboxField.class,
        User.class, Schema.class, Category.class, Datasource.class, Option.class);

    config.setReturnBodyOnCreate(true);
    config.setReturnBodyOnUpdate(true);
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