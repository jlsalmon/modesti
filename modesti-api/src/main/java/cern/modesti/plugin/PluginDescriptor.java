package cern.modesti.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Justin Lewis Salmon
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginDescriptor {
  private String name;
  private String version;
}
