package cern.modesti.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@Configuration
@Profile({"dev", "test", "prod"})
public class JpaConfig {

  @Bean(destroyMethod = "close")
  @Primary
  @ConfigurationProperties(prefix = "modesti.jdbc")
  public DataSource coreDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "modesti.jdbc")
  public LocalContainerEntityManagerFactoryBean coreEntityManagerFactory(EntityManagerFactoryBuilder builder) {
    return  builder.dataSource(coreDataSource()).packages("cern.modesti").persistenceUnit("core").build();
  }

  @Bean
  @Primary
  public JpaTransactionManager coreTransactionManager(EntityManagerFactoryBuilder builder) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(coreEntityManagerFactory(builder).getObject());
    return transactionManager;
  }
}
