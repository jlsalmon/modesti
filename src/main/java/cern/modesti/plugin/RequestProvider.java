package cern.modesti.plugin;

import cern.modesti.plugin.metadata.MetadataProvider;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import cern.modesti.request.upload.parser.RequestParser;
import cern.modesti.workflow.validation.RequestValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

import static java.lang.String.format;

/**
 * An implementation must extend this class to be registered as a plugin.
 *
 * @author Justin Lewis Salmon
 */
public abstract class RequestProvider implements Plugin<Request>, MetadataProvider {

  /**
   * Decides if a plugin should be invoked according to the given request.
   *
   * @param request the request instance
   * @return true if the plugin should be invoked for this request, false otherwise
   */
  @Override
  public boolean supports(Request request) {
    return getMetadata().getName().equals(request.getDomain());
  }

  /**
   * A plugin should implement this method to provide search capability on its
   * domain database.
   *
   * @param query    an RSQL query string representing the user search input
   * @param pageable paging information
   * @return a list of {@link Point} instances matching the given query
   */
  public abstract Page<Point> findAll(String query, Pageable pageable);

  /**
   * Plugins that wish to provide upload support from Excel sheets must
   * override this method and return a {@link RequestParser} implementation.
   *
   * @return a {@link RequestParser} capable of parsing a {@link Request}
   * instance of this domain from an Excel sheet.
   */
  public RequestParser getRequestParser() {
    throw new UnsupportedRequestException(format("Plugin for domain %s does not provide a RequestParser implementation", getMetadata().getName()));
  }

  public RequestValidator getRequestValidator() {
    return null;
  }
}
