package cern.modesti.request;

import org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Justin Lewis Salmon
 */
@ControllerAdvice(basePackageClasses = RepositoryRestExceptionHandler.class)
public class RequestExceptionHandler {

  @ExceptionHandler
  ResponseEntity handle(Exception e) {
    return new ResponseEntity(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }
}
