package cern.modesti.workflow;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * This class is responsible for finding BPMN process definitions on the
 * classpath and creating deployments for them.
 * <p>
 * Process definitions are loaded from directories named {@literal processes}
 * at the root of the classpath, and must have a {@literal .bpmn20.xml}
 * extension.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class WorkflowInitialiser {

  private static final String PROCESS_RESOURCE_PATTERN = "classpath*:/processes/*.bpmn20.xml";

  @Autowired
  private RepositoryService repositoryService;

  @PostConstruct
  public void init() throws IOException {
    log.info("Initialising workflow processes");

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource[] processes = resolver.getResources(PROCESS_RESOURCE_PATTERN);

    for (Resource process : processes) {
      repositoryService.createDeployment().addInputStream(process.getFilename(), process.getInputStream()).deploy();
    }
  }
}
