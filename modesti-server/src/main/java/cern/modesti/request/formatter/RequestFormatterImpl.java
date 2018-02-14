package cern.modesti.request.formatter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import cern.modesti.request.Request;
import cern.modesti.request.RequestFormatter;
import lombok.extern.slf4j.Slf4j;

/**
 * Primary implementation of the {@link RequestFormatter}
 * 
 * It looks for a formatter applicable to the request domain and applies the formatting.
 * 
 * @author Ivan Prieto Barreiro
 */
@Component
@Primary
@Slf4j
public class RequestFormatterImpl implements RequestFormatter {

  @Autowired
  private ApplicationContext context;
  
  private static final String SUFFIX = "RequestFormatter";
  
  @Override
  public void format(Request request) {
    String formatterName = request.getDomain() + SUFFIX;
    try {
      RequestFormatter domainFormatter = context.getBean(formatterName, RequestFormatter.class);
      domainFormatter.format(request);
    } catch (BeansException e) {
      // Domain formatter not found
      log.debug("Exception looking for a request formatter of the domain {}", formatterName, e);
    }
  }
}
