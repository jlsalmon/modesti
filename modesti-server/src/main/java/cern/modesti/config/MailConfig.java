package cern.modesti.config;

import org.springframework.beans.factory.annotation.Value;
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
  
  @Value("${spring.mail.port:25000}")
  private int port;

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Wiser wiser() {
    Wiser wiser = new Wiser();
    wiser.setPort(port);
    wiser.setHostname("localhost");
    return wiser;
  }
}
