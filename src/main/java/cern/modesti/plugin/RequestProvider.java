package cern.modesti.plugin;

import cern.modesti.request.Request;
import cern.modesti.request.RequestType;
import org.springframework.plugin.core.Plugin;

/**
 * TODO
 *
 * TODO get uploaded request parser via plugin method?
 * TODO get schemas and workflow processes via classpath?
 * TODO what about configuration of additional DB connections inside plugins?
 *
 * @author Justin Lewis Salmon
 */
public interface RequestProvider extends Plugin<Request> {

  /**
   * Validate the given request.
   *
   * @param request the request to validate
   * @return true if the request is valid, false otherwise
   */
  boolean validate(Request request);

  /**
   * Configure the given request on the target system.
   *
   * @param request the request to configure
   * @return true if the request was configured successfully, false otherwise
   */
  boolean configure(Request request);

  /**
   * Retrieve a workflow process key based on the request type.
   *
   * @param type the type of the request
   * @return the workflow process key
   */
  String getProcessKey(RequestType type);

  /**
   * Returns if a plugin should be invoked according to the given request.
   *
   * @param request
   * @return true if the plugin should be invoked for this request, false otherwise
   */
  @Override
  boolean supports(Request request);
}
