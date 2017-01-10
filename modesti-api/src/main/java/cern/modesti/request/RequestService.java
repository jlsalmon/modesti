package cern.modesti.request;

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
}
