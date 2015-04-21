package cern.modesti.config;

import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableJpaRepositories(basePackages="cern.modesti.repository.jpa")
public class JpaConfig {

  @Autowired
  private Environment env;
  @Autowired
  private DataSource dataSource;

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

    HibernateJpaVendorAdapter hibernateJpa = new HibernateJpaVendorAdapter();
    hibernateJpa.setDatabasePlatform(env.getProperty("hibernate.dialect"));
    hibernateJpa.setShowSql(env.getProperty("hibernate.show_sql", Boolean.class));
    hibernateJpa.setGenerateDdl(true);

    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    emf.setDataSource(dataSource);
    emf.setPackagesToScan("cern.modesti");
    emf.setJpaVendorAdapter(hibernateJpa);
    emf.setJpaPropertyMap(Collections.singletonMap("javax.persistence.validation.mode", "none"));
    return emf;
  }

  @Bean
  public JpaTransactionManager transactionManager() {
    JpaTransactionManager txnMgr = new JpaTransactionManager();
    txnMgr.setEntityManagerFactory(entityManagerFactory().getObject());
    return txnMgr;
  }

}
