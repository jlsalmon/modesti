package cern.modesti.config.init;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import cern.modesti.model.Domain;
import cern.modesti.repository.mongo.domain.DomainRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

/**
 * This class will delete and re-add all available domains from the domain
 * repository.
 *
 * @author Justin Lewis Salmon
 *
 */
@Service
@Profile("test")
public class DomainInitialiser {
  private static final Logger LOG = LoggerFactory.getLogger(DomainInitialiser.class);

  private static final String DOMAIN_RESOURCE = "classpath:data/domains.json";

  @Autowired
  public DomainInitialiser(DomainRepository domainRepository) throws IOException {
    LOG.info("Initialising domains and datasources");
    ObjectMapper mapper = new ObjectMapper();

    domainRepository.deleteAll();

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
    Resource schemas = resolver.getResource(DOMAIN_RESOURCE);

    byte[] bytes = ByteStreams.toByteArray(schemas.getInputStream());
    List<Domain> domains = mapper.readValue(new String(bytes, StandardCharsets.UTF_8), new TypeReference<List<Domain>>() {});

    domainRepository.insert(domains);
  }

}
