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
  private String name;
  private String version;
  private String homePage;
  private List<PluginDescriptor> plugins;
}
