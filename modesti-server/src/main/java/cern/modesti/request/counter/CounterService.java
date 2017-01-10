package cern.modesti.request.counter;

import cern.modesti.request.Request;

/**
 * Service class for generating IDs from database counters (sequences).
 *
 * @author Justin Lewis Salmon
 */
public interface CounterService {

  /**
   * Identifier for the ID sequence for {@link Request}
   * instances.
   */
  String REQUEST_ID_SEQUENCE = "requests";

  /**
   * Retrieve the next value from a sequence.
   *
   * @param collectionName the collection for which to generate a sequence
   *                       value
   * @return the next value from the requested sequence
   */
  Long getNextSequence(String collectionName);
}
