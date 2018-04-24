package cern.modesti.schema;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Justin Lewis Salmon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowCommentStateDescriptor implements Serializable {

  private static final long serialVersionUID = -2517661941577490113L;
  
  private String status;
  private String property;
}
