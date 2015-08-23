package cern.modesti.request.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import javax.persistence.Id;

/**
 *
 * @author Justin Lewis Salmon
 *
 */
@Data
@NoArgsConstructor
public class Domain {

  private String name;

  private List<String> datasources;

  private List<String> categories;

  public Domain(String name) {
    this.name = name;
  }
}
