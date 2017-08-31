package cern.modesti.request;

import cern.modesti.user.User;
import cern.modesti.workflow.request.RequestAction;

/**
 * Service class for creating, updating, deleting and searching for
 * {@link Request} objects.
 *
 * @author Justin Lewis Salmon
 */
public interface RequestService {

  Request insert(Request request);

  Request save(Request request);

  void delete(Request request);

  Request findOneByRequestId(String requestId);
  
  /**
   * Execute an action on a {@link Request} inside a workflow process instance.
   *
   * @param requestId the id of the request
   * @param action    the action to execute
   * @param user      the user who is performing the action
   * @return the updated request 
   */
  Request execute(String requestId, RequestAction action, User user);
}
