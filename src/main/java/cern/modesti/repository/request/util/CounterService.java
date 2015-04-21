/**
 *
 */
package cern.modesti.repository.request.util;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface CounterService {

  Long getNextSequence(String collectionName);
}
