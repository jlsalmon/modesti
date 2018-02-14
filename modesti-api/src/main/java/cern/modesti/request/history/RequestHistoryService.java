package cern.modesti.request.history;

import cern.modesti.request.Request;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestHistoryService {

  /**
   * Get the list of changes for the given request.
   * @param request the request object
   * @return
   */
  List<Change> getChanges(Request request);
  
  /**
   * Create a new entry in the history repository for the given request.
   *
   * @param request the request object
   */
  void initialiseChangeHistory(Request request);
  
  /**
   * Compare the changes from the given, modified request to the original
   * request and save them to the history record.
   *
   * Currently we are only storing a single change event, diffed from the
   * original request. This only makes sense for UPDATE requests. Storing
   * successive diffs for CREATE requests would probably be too complicated.
   *
   * @param modified the modified request
   */
  void saveChangeHistory(Request modified);
  
  
  /**
   * Delete the change history of a request.
   * @param request the request object
   */
  void deleteChangeHistory(Request request);
}
