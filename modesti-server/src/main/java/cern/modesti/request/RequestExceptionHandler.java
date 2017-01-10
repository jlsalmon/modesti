package cern.modesti.request;

import cern.modesti.workflow.task.NotAuthorisedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.data.rest.webmvc.support.ExceptionMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles exceptions thrown from inside {@link RequestRepositoryEventHandler}.
 *
 * @author Justin Lewis Salmon
 */
@ControllerAdvice(basePackageClasses = RepositoryRestExceptionHandler.class)
@Slf4j
public class RequestExceptionHandler {

  @ExceptionHandler
  ResponseEntity<ExceptionMessage> handleNotAuthorised(NotAuthorisedException e) {
    return errorResponse(HttpStatus.FORBIDDEN, new HttpHeaders(), e);
  }

  private static ResponseEntity<ExceptionMessage> errorResponse(HttpStatus status, HttpHeaders headers, Exception exception) {
    if (exception != null) {

      String message = exception.getMessage();
      log.error(message, exception);

      if (StringUtils.hasText(message)) {
        return response(status, headers, new ExceptionMessage(exception));
      }
    }

    return response(status, headers, null);
  }

  private static <T> ResponseEntity<T> response(HttpStatus status, HttpHeaders headers, T body) {
    Assert.notNull(headers, "Headers must not be null!");
    Assert.notNull(status, "HttpStatus must not be null!");

    return new ResponseEntity<T>(body, headers, status);
  }
}
