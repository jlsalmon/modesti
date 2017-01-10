package cern.modesti.request;

/**
 * @author Justin Lewis Salmon
 */
public interface RequestFactory {

  static Request createRequest() {
    Request request;

    try {
      request = (Request) Class.forName("cern.modesti.request.RequestImpl").newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException("Error creating Request instance", e);
    }

    return request;
  }
}

