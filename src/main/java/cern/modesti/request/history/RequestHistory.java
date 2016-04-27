package cern.modesti.request.history;

import cern.modesti.request.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@Document(collection = "request.history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestHistory {

  @Id
  private String id;

  private String requestId;

  private Request originalRequest;

  private List<ChangeEvent> events = new ArrayList<>();

  private boolean deleted;
}