package cern.modesti.request;

/**
 * SPI for handling create/update/delete events of {@link Request} instances.
 *
 * @author Justin Lewis Salmon
 */
public interface RequestEventHandler {

  void onBeforeSave(Request request);
}
