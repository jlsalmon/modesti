package cern.modesti.plugin;

import cern.modesti.plugin.metadata.MetadataProvider;
import cern.modesti.request.Request;
import cern.modesti.request.point.Point;
import cern.modesti.upload.parser.RequestParser;
import com.mysema.query.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

import static java.lang.String.format;

/**
 *
 * @author Justin Lewis Salmon
 */
public abstract class RequestProvider implements Plugin<Request>, MetadataProvider {

  /**
   * Decides if a plugin should be invoked according to the given request.
   *
   * @param request
   *
   * @return true if the plugin should be invoked for this request, false otherwise
   */
  @Override
  public boolean supports(Request request) {
    return getMetadata().getName().equals(request.getDomain());
  }

  /**
   * Find a set of points
   *
   * @param query
   * @param pageable
   * @return
   */
  public abstract Page<Point> findAll(String query, Pageable pageable);

  /**
   * Plugins that wish to provide upload support from Excel sheets must override this method and return a {@link RequestParser} implementation.
   *
   * @return
   */
  public RequestParser getRequestParser() {
    throw new UnsupportedRequestException(format("Plugin for domain %s does not provide a RequestParser implementation", getMetadata().getName()));
  }
}
