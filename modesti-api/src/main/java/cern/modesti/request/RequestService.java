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

  /**
   * Insert (create) a new request.
   * <p>
   * Creating a new request performs the following actions:
   * <ul>
   * <li>
   * Asserts that the currently logged-in user is authorised to create a
   * request for the domain of the request
   * </li>
   * <li>
   * Sets the currently logged-in user as the creator of the request
   * </li>
   * <li>Generates a request id</li>
   * <li>Adds some empty points to the request if none were specified</li>
   * <li>Starts a new workflow process instance using the workflow key of the
   * plugin associated with the request domain</li>
   * </ul>
   *
   * @param request the request to create
   * @return the newly created request with all properties set
   */
  Request insert(Request request);

  /**
   * Save an existing request.
   *
   * @param request the request to save
   * @return the newly saved request
   */
  Request save(Request request);

  /**
   * Delete a request.
   *
   * @param request the request to delete
   */
  void delete(Request request);

  /**
   * Find a single request.
   *
   * @param requestId the id of the request
   * @return the request instance, or null if no request was found with the
   * given id
   */
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
