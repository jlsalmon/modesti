package cern.modesti.schema.field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Justin Lewis Salmon
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Option implements Serializable {

  private static final long serialVersionUID = 728578311555988751L;

  private Object value;
  private Object description;
}
