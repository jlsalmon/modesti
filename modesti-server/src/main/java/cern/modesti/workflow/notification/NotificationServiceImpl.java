package cern.modesti.workflow.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static java.lang.String.format;

/**
 * Service class for sending {@link Notification} instances from within
 * workflow methods.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private SpringTemplateEngine templateEngine;

  @Autowired
  private Environment env;

  /**
   * Send a notification.
   *
   * @param notification the notification to send
   */
  public void sendNotification(Notification notification) {
    List<String> recipients = new ArrayList<>();

    // Build the list of recipient addresses
    for (String recipient : notification.getRecipients()) {
      InternetAddress email;
      try {
        email = new InternetAddress(recipient);
        email.validate();
        recipients.add(recipient);
      } catch (AddressException e) {
        throw new IllegalArgumentException("Not a valid email address: " + recipient);
      }
    }

    if (recipients.isEmpty()) {
      throw new IllegalArgumentException("Notification must specify one or more recipients");
    }

    String from = env.getRequiredProperty("spring.mail.from");

    // If we're on the test server, send all emails to the addresses specified in the property or to the developers group
    if (from.contains("test")) {
      String [] sendTo = env.getProperty("spring.mail.test.recipients", "modesti-developers@cern.ch").split(",");
      recipients = Arrays.asList(sendTo);
      from = "modesti.service@cern.ch";
    }

    String subject = notification.getSubject();
    if (subject == null) throw new IllegalArgumentException("Notification subject must not be null");

    String template = notification.getTemplate();
    if (template == null) throw new IllegalArgumentException("Notification template must not be null");

    // Prepare the evaluation context
    final Context context = new Context(Locale.UK);

    // Set default context variables
    context.setVariable("request", notification.getRequest());
    context.setVariable("schema", notification.getSchema());
    context.setVariable("url", env.getRequiredProperty("modesti.base") + "/requests/" + notification.getRequest().getRequestId());

    // Set user variables
    for (Map.Entry<String, Object> entry : notification.getTemplateParameters().entrySet()) {
      context.setVariable(entry.getKey(), entry.getValue());
    }

    send(recipients, from, format("[MODESTI] %s", subject), template, context);
  }

  private void send(List<String> to, String from, String subject, String template, Context context) {
    // Prepare message using a Spring helper
    final MimeMessage mimeMessage = mailSender.createMimeMessage();
    final MimeMessageHelper message;

    try {
      message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      message.setSubject(subject);

      message.setTo(to.toArray(new String[to.size()]));
      message.setFrom(from);

      // Create the HTML body using Thymeleaf
      final String htmlContent = templateEngine.process(template, context);
      message.setText(htmlContent, true);

    } catch (MessagingException e) {
      log.error("Error sending email notification", e);
    }

    // Send mail
    mailSender.send(mimeMessage);
  }
}
