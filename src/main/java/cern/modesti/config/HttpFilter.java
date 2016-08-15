package cern.modesti.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * {@link Filter} implementation that forwards non-API requests to the index
 * page for the frontend to handle.
 *
 * @author Justin Lewis Salmon
 */
@Component
public class HttpFilter extends OncePerRequestFilter {

  private Pattern pattern = Pattern.compile("/[^\\.]*");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    if (this.pattern.matcher(request.getRequestURI()).matches() && !request.getRequestURI().startsWith("/api")) {
      // Forward to home page so that route is preserved.
      request.getRequestDispatcher("/").forward(request, response);
    } else {
      filterChain.doFilter(request, response);
    }
  }
}
