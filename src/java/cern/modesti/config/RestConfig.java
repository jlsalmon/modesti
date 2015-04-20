package cern.modesti.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.rest.SpringBootRepositoryRestMvcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.validation.Validator;

import cern.modesti.model.AlarmCategory;
import cern.modesti.model.AlarmPriority;
import cern.modesti.model.DataType;
import cern.modesti.model.Person;
import cern.modesti.model.Point;
import cern.modesti.model.Site;
import cern.modesti.repository.request.SearchTextConverter;
import cern.modesti.repository.request.schema.field.OptionsField;
import cern.modesti.repository.request.schema.field.TextField;
import cern.modesti.repository.request.schema.field.TypeaheadField;

@Configuration
public class RestConfig extends SpringBootRepositoryRestMvcConfiguration {

  @Autowired
  private Validator validator;

  @Override
  protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
    validatingListener.addValidator("beforeCreate", validator);
    validatingListener.addValidator("beforeSave", validator);
  }

  @Override
  protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    super.configureRepositoryRestConfiguration(config);

    // Tell Spring Data REST to expose IDs for the following classes in JSON
    // responses.
    config.exposeIdsFor(Point.class, Person.class, Site.class, DataType.class, AlarmCategory.class, AlarmPriority.class, TextField.class, OptionsField.class,
        TypeaheadField.class);

    config.setReturnBodyOnCreate(true);
    config.setReturnBodyOnUpdate(true);
  }

  @Bean
  public SearchTextConverter searchTextConverter() {
    return new SearchTextConverter();
  }

  @Override
  protected void configureConversionService(ConfigurableConversionService conversionService) {
    conversionService.addConverter(searchTextConverter());
  }
}