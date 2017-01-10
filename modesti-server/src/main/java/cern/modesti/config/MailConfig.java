package cern.modesti.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.subethamail.wiser.Wiser;

/**
 * Configuration class to create an embedded SMTP server for development.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Profile("dev")
public class MailConfig {

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Wiser wiser() {
    Wiser wiser = new Wiser();
    wiser.setPort(25000);
    wiser.setHostname("localhost");
    return wiser;
  }
}
