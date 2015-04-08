package cern.modesti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import cern.modesti.Application.CustomRepositoryRestMvcConfiguration;

@SpringBootApplication
//@EnableElasticsearchRepositories(basePackages = "cern/modesti/elastic")
@Import(CustomRepositoryRestMvcConfiguration.class)
public class Application {

  static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

//    BuildingRepository repository = context.getBean(BuildingRepository.class);
//    logger.info(repository.toString());
//    ElasticsearchOperations eo = context.getBean(ElasticsearchOperations.class);
//
//    repository.index(new Building("1", "Restaurant 1", "500"));

  }

  @Bean
  public ValidatingMongoEventListener validatingMongoEventListener() {
    return new ValidatingMongoEventListener(validator());
  }

  @Bean
  public LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }

  // @Configuration
  // protected static class ElasticsearchConfiguration {
  //
  // @Bean(name = "elasticsearchTemplate")
  // public ElasticsearchOperations elasticsearchTemplate() {
  // logger.info("creating es template bean");
  // NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
  // nodeBuilder.settings().put("http.enabled", true);
  // return new
  // ElasticsearchTemplate(nodeBuilder.local(true).clusterName("modesti.elasticsearch").node().client());
  // }
  // }

//  @Configuration
//  @EnableConfigurationProperties(ElasticsearchProperties.class)
//  protected static class ElasticsearchConfiguration implements DisposableBean {
//
//    private final Logger logger = LoggerFactory.getLogger(ElasticsearchConfiguration.class);
//
//    @Autowired
//    private ElasticsearchProperties properties;
//
//    private NodeClient client;
//
//    @Bean
//    public ElasticsearchTemplate elasticsearchTemplate() {
//      return new ElasticsearchTemplate(esClient());
//    }
//
//    @Bean
//    public Client esClient() {
//      try {
//        if (logger.isInfoEnabled()) {
//          logger.info("Starting Elasticsearch client");
//        }
//        NodeBuilder nodeBuilder = new NodeBuilder();
//        nodeBuilder.clusterName(this.properties.getClusterName()).local(false);
//        nodeBuilder.settings().put("http.enabled", true);
//        this.client = (NodeClient) nodeBuilder.node().client();
//        return this.client;
//      } catch (Exception ex) {
//        throw new IllegalStateException(ex);
//      }
//    }
//
//    @Override
//    public void destroy() throws Exception {
//      if (this.client != null) {
//        try {
//          if (logger.isInfoEnabled()) {
//            logger.info("Closing Elasticsearch client");
//          }
//          if (this.client != null) {
//            this.client.close();
//          }
//        } catch (final Exception ex) {
//          if (logger.isErrorEnabled()) {
//            logger.error("Error closing Elasticsearch client: ", ex);
//          }
//        }
//      }
//    }
//  }

  /**
   * Allows validation errors to be converted to REST responses
   *
   * @author Justin Lewis Salmon
   */
  @Configuration
  protected static class CustomRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {

    @Autowired
    private Validator validator;

    @Override
    protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
      validatingListener.addValidator("beforeCreate", validator);
      validatingListener.addValidator("beforeSave", validator);
    }

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
      super.configureRepositoryRestConfiguration(config);
      config.exposeIdsFor(Point.class);
      config.setReturnBodyOnCreate(true);
      config.setReturnBodyOnUpdate(true);
    }

    @Bean
    public SearchTextConverter searchTextConverter() {
      return new SearchTextConverter();
    }

    @Override
    protected void configureConversionService(ConfigurableConversionService conversionService) {
      conversionService.addConverter(searchTextConverter());
    }
  }
}
