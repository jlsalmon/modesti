package cern.modesti.config;

import cern.modesti.repository.jpa.alarm.AlarmCategory;
import cern.modesti.repository.jpa.equipment.MonitoringEquipment;
import cern.modesti.repository.jpa.gmao.GmaoCode;
import cern.modesti.repository.jpa.location.BuildingName;
import cern.modesti.repository.jpa.location.Location;
import cern.modesti.repository.jpa.location.functionality.Functionality;
import cern.modesti.repository.jpa.location.zone.Zone;
import cern.modesti.repository.jpa.person.Person;
import cern.modesti.repository.jpa.subsystem.SubSystem;
import cern.modesti.request.SearchTextConverter;
import cern.modesti.request.point.Point;
import cern.modesti.schema.field.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.rest.SpringBootRepositoryRestMvcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.validation.Validator;

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

    // Tell Spring Data REST to expose IDs for the following classes in JSON responses.
    config.exposeIdsFor(Point.class, Person.class, Functionality.class, Location.class, Zone.class, SubSystem.class, AlarmCategory.class, Field.class, TextField
        .class, OptionsField.class, AutocompleteField.class, NumericField.class, CheckboxField.class, BuildingName.class, GmaoCode.class, MonitoringEquipment
        .class);

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