package cern.modesti.request.counter;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Justin Lewis Salmon
 */
@Document(collection = "counters")
public class Counter {

  @Id
  private String id;

  private Long sequence;

  /**
   *
   */
  public Counter(String id, Long sequence) {
    this.id = id;
    this.sequence = sequence;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the sequence
   */
  public Long getSequence() {
    return sequence;
  }

  /**
   * @param sequence the sequence to set
   */
  public void setSequence(Long sequence) {
    this.sequence = sequence;
  }
}