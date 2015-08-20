package cern.modesti.config.init;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@Profile({"test", "dev", "prod"})
public class WorkflowProcessInitialiser {

  private static final String PROCESS_RESOURCE_PATTERN = "classpath*:/processes/*.bpmn20.xml";

  @Autowired
  public WorkflowProcessInitialiser(RepositoryService repositoryService) throws IOException {
    log.info("Initialising workflow processes");

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource[] processes = resolver.getResources(PROCESS_RESOURCE_PATTERN);

    for (Resource process : processes) {
      repositoryService.createDeployment().addInputStream(process.getFilename(), process.getInputStream()).deploy();
    }
  }
}
