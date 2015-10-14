package cern.modesti.request;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestEventHandler {

  void onBeforeSave(Request request);
}
