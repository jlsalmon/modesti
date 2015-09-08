package cern.modesti.request;

import cern.modesti.repository.alarm.AlarmCategory;
import cern.modesti.repository.equipment.MonitoringEquipment;
import cern.modesti.repository.gmao.GmaoCode;
import cern.modesti.repository.location.BuildingName;
import cern.modesti.repository.location.Location;
import cern.modesti.repository.location.functionality.Functionality;
import cern.modesti.repository.location.zone.SafetyZone;
import cern.modesti.repository.person.Person;
import cern.modesti.repository.subsystem.SubSystem;
import cern.modesti.request.point.Point;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
public class RequestDeserialiser extends JsonDeserializer<Request> {

  @Override
  public Request deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.readValue(parser, Request.class);
  }

  @Override
  public Request deserialize(JsonParser parser, DeserializationContext context, Request requestToUpdate) throws IOException {
    log.debug(format("updating request %s...", requestToUpdate.getRequestId()));

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Request updated = mapper.readValue(parser, Request.class);

    // Make sure all the nested object properties (gmaoCode, etc.) are properly deserialised.

    for (Point point : updated.getPoints()) {
      Map<String, Object> properties = point.getProperties();

      if (properties.get("gmaoCode") != null) properties.put("gmaoCode", mapper.convertValue(properties.get("gmaoCode"), GmaoCode.class));
      if (properties.get("responsiblePerson") != null) properties.put("responsiblePerson", mapper.convertValue(properties.get("responsiblePerson"), Person.class));
      if (properties.get("subsystem") != null)properties.put("subsystem", mapper.convertValue(properties.get("subsystem"), SubSystem.class));
      if (properties.get("monitoringEquipment") != null)properties.put("monitoringEquipment", mapper.convertValue(properties.get("monitoringEquipment"), MonitoringEquipment.class));
      if (properties.get("location") != null)properties.put("location", mapper.convertValue(properties.get("location"), Location.class));
      if (properties.get("buildingName") != null)properties.put("buildingName", mapper.convertValue(properties.get("buildingName"), BuildingName.class));
      if (properties.get("functionality") != null) properties.put("functionality", mapper.convertValue(properties.get("functionality"), Functionality.class));
      if (properties.get("safetyZone") != null)properties.put("safetyZone", mapper.convertValue(properties.get("safetyZone"), SafetyZone.class));
      if (properties.get("alarmCategory") != null)properties.put("alarmCategory", mapper.convertValue(properties.get("alarmCategory"), AlarmCategory.class));

      // TODO remove this domain-specific code, possibly by introducing a type registry for plugins
      if (properties.get("csamCsename") != null)properties.put("csamCsename", mapper.convertValue(properties.get("csamCsename"), GmaoCode.class));
    }

    updated.setId(requestToUpdate.getId());
    BeanUtils.copyProperties(updated, requestToUpdate);
    return updated;
  }
}
