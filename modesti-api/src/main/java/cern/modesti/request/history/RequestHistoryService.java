package cern.modesti.request.history;

import cern.modesti.request.Request;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestHistoryService {

  List<Change> getChanges(Request request);
}
