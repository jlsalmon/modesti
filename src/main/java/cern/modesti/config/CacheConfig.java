package cern.modesti.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableCaching
@Profile({"dev", "test", "prod"})
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    // TODO: remove this
    return new ConcurrentMapCacheManager("gmaoCodes", "locations", "functionalities", "zones", "persons", "subsystems");
  }
}
