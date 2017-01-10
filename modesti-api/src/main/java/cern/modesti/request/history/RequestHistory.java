package cern.modesti.request.history;

import cern.modesti.request.Request;

import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestHistory {
  String getId();

  String getRequestId();

  Request getOriginalRequest();

  String getIdProperty();

  void setEvents(List<ChangeEvent> events);

  List<ChangeEvent> getEvents();
}
