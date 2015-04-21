/**
 *
 */
package cern.modesti.repository.mongo.request.counter;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface CounterService {

  Long getNextSequence(String collectionName);
}
