package cern.modesti.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

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
    // Register a deserialiser for Joda classes
    mapper.registerModule(new JodaModule());

    Request updated = mapper.readValue(parser, Request.class);

    // The request id may not be modified manually.
    if (!Objects.equals(updated.getRequestId(), requestToUpdate.getRequestId())) {
      throw new IllegalArgumentException("Request ID cannot not be updated manually!");
    }

    // The request status may not be modified manually.
    if (!Objects.equals(updated.getStatus(), requestToUpdate.getStatus())) {
      throw new IllegalArgumentException("Request status cannot not be updated manually!");
    }

    // TODO: this shouldn't be necessary, and could cause side effects. Why do we lose properties when saving?
    Map<String, Object> properties = requestToUpdate.getProperties();
    properties.putAll(updated.getProperties());
    updated.setProperties(properties);

    updated.setId(requestToUpdate.getId());
    BeanUtils.copyProperties(updated, requestToUpdate);
    return updated;
  }
}
