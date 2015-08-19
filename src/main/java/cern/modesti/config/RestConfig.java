package cern.modesti.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.rest.SpringBootRepositoryRestMvcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.validation.Validator;

import cern.modesti.repository.jpa.alarm.AlarmCategory;
import cern.modesti.repository.jpa.equipment.MonitoringEquipment;
import cern.modesti.repository.jpa.gmao.GmaoCode;
import cern.modesti.repository.jpa.location.BuildingName;
import cern.modesti.repository.jpa.location.Location;
import cern.modesti.repository.jpa.location.functionality.Functionality;
import cern.modesti.repository.jpa.location.zone.SafetyZone;
import cern.modesti.repository.jpa.person.Person;
import cern.modesti.repository.jpa.subsystem.SubSystem;
import cern.modesti.request.SearchTextConverter;
import cern.modesti.request.point.Point;
import cern.modesti.schema.field.AutocompleteField;
import cern.modesti.schema.field.CheckboxField;
import cern.modesti.schema.field.Field;
import cern.modesti.schema.field.NumericField;
import cern.modesti.schema.field.OptionsField;
import cern.modesti.schema.field.TextField;
import cern.modesti.security.ldap.User;

@Configuration
@EnableEntityLinks
@Profile({"test", "dev", "prod"})
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
    config.exposeIdsFor(Point.class, Person.class, Functionality.class, Location.class, SafetyZone.class, SubSystem.class, AlarmCategory.class, Field.class, TextField
        .class, OptionsField.class, AutocompleteField.class, NumericField.class, CheckboxField.class, BuildingName.class, GmaoCode.class, MonitoringEquipment
        .class, User.class);

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