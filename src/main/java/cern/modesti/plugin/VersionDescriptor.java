package cern.modesti.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionDescriptor {
  private String version;
  private List<PluginDescriptor> plugins;
}
