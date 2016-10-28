package cern.modesti.plugin.metadata;

import cern.modesti.request.Request;
import cern.modesti.request.RequestType;

/**
 * Basic interface to define a set of metadata information for plugins.
 *
 * @author Justin Lewis Salmon
 */
public interface PluginMetadata {

  /**
   * Returns the unique plugin Id. Plugins returning a metadata implementation
   * have to ensure uniqueness of this name.
   *
   * @return the name of the plugin
   */
  String getId();

  /**
   * Returns the name of the group to which a user must belong in order to be
   * authorised to use this plugin.
   *
   * @param request the request to authorise, which may be used to determine
   *                the authorisation group
   *
   * @return the authorisation group name
   */
  String getAuthorisationGroup(Request request);

  /**
   * Returns the identifier of the workflow process to be used based on the
   * given request type.
   *
   * @param type the type of the request
   * @return the workflow process identifier mapping to the given request type
   */
  String getProcessKey(RequestType type);
}
