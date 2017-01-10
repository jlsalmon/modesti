package cern.modesti.request.counter;

import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Justin Lewis Salmon
 */
@Document(collection = "counters")
@Data
@AllArgsConstructor
public class Counter {

  @Id
  private String id;
  private Long sequence;
}