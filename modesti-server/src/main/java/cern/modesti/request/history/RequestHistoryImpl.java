package cern.modesti.request.history;

import cern.modesti.request.Request;
import cern.modesti.request.RequestImpl;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the entire history of a single request. Each change to
 * a request is recorded as a successive diff.
 *
 * @author Justin Lewis Salmon
 */
@Document(collection = "request.history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestHistoryImpl implements RequestHistory {

  @Id
  private String id;

  private String requestId;

  private String idProperty;

  @QueryType(PropertyType.NONE)
  @JsonDeserialize(as = RequestImpl.class)
  private Request originalRequest;

  private List<ChangeEvent> events = new ArrayList<>();

  private boolean deleted;
}
