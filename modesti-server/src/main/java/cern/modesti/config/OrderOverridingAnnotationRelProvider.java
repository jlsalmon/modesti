package cern.modesti.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.data.rest.core.support.RepositoryRelProvider;
import org.springframework.stereotype.Component;
import org.springframework.hateoas.core.Relation;

/**
 * This is a workaround to make sure that the {@link AnnotationRelProvider} is
 * used with the correct priority. Otherwise, the {@link RepositoryRelProvider}
 * gets used instead which makes {@link Relation} annotations useless.
 *
 * This class can be removed once https://jira.spring.io/browse/DATAREST-904
 * is resolved.
 *
 * @author Justin Lewis Salmon
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE + 9)
public class OrderOverridingAnnotationRelProvider implements RelProvider {

  private AnnotationRelProvider annotationRelProvider;

  @Autowired
  private OrderOverridingAnnotationRelProvider(GenericApplicationContext context) {
    this.annotationRelProvider = context.getBean("annotationRelProvider", AnnotationRelProvider.class);
  }

  @Override
  public String getItemResourceRelFor(Class<?> type) {
    return annotationRelProvider.getItemResourceRelFor(type);
  }

  @Override
  public String getCollectionResourceRelFor(Class<?> type) {
    return annotationRelProvider.getCollectionResourceRelFor(type);
  }

  @Override
  public boolean supports(Class<?> delimiter) {
    return annotationRelProvider.supports(delimiter);
  }
}
